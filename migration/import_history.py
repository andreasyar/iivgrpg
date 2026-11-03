"""Rebuild in an EMPTY target directory. Never writes to the legacy archive."""
import sys,subprocess,datetime,io,os
from analyze import *

STAMP=datetime.datetime.now(datetime.timezone.utc).isoformat()
HERE=Path(__file__).resolve().parent
TARGET=Path(sys.argv[1] if len(sys.argv)>1 else '/tmp/iivgrpg-git-import')
if TARGET.exists() and any(TARGET.iterdir()):raise SystemExit('Target must be empty')
TARGET.mkdir(exist_ok=True)
subprocess.run(['git','init','-b','main',str(TARGET)],check=True,capture_output=True)
def git(*a,input=None):return subprocess.check_output(['git','-C',str(TARGET),*a],input=input)
def timestamp(s):return str(int(datetime.datetime.fromisoformat(s.replace('Z','+00:00')).timestamp()))+' +0000'
hist,revs,analysis,known=analyze()
assert set(revs)==set(range(130))
reference=json.loads((ROOT/'google-code/revision-log.json').read_text())['revisions']
assert {x['revision']:x['properties'] for x in reference}==revs
out=io.BytesIO();marks={};mark=0

def raw(s):out.write(s.encode() if isinstance(s,str) else s)
def data(b):raw(f'data {len(b)}\n');raw(b);raw('\n')
def commit(branch,tree,message,date,who,parent=None):
    global mark
    mark+=1;m=mark
    raw(f'commit refs/heads/{branch}\nmark :{m}\n')
    raw(f'author {who} {timestamp(date)}\ncommitter {who} {timestamp(date)}\n')
    data(message)
    if parent is not None:raw(f'from :{parent}\n')
    raw('deleteall\n')
    for p,b in sorted(tree.items()):
        assert not excluded(p),p
        raw('M 100644 inline '+json.dumps(p)+'\n');data(b)
    raw('\n');return m

for r in range(1,130):
    p=revs[r];author=p.get('svn:author',p.get('google:author','unknown'))
    who=f'{author.split("@")[0]} <{author}>' if '@' in author else f'{author} <{author}@authors.invalid>'
    marks[r]=commit('main',hist[r],p['svn:log'].encode(),p['svn:date'],who,marks.get(r-1))

# Preserve only useful variants absent from EVERY SVN revision; normalize only for comparison.
seen={};branch_info=[];selected=[(0,'local-r129','Поздние локальные карты и настройки клиентского проекта.'),(1,'netbeans-server','Ранняя сборка сервера Ant/NetBeans и вариант кэша карт.'),(2,'map-events','Локальные события клеток и эксперимент перехода между картами.'),(5,'netbeans-server-later','Другой вариант сборки сервера NetBeans.'),(8,'client-conflicts','Незавершённые SVN-конфликты и локальные .mine; исторические материалы для ручного разбора.')]
for idx,name,description in selected:
    entry=analysis[idx];tree={};provenance=[]
    # Choose nearest existing SVN snapshot by matching files at the same logical paths.
    local={x['path']:norm((ROOT/x['source']).read_bytes()) for x in entry['files'] if x['classification']=='in-svn'}
    base=max(range(1,130),key=lambda r:(sum(hist[r].get(p,b'').replace(b'\r\n',b'\n')==b for p,b in local.items()),r)) if local else None
    for x in entry['files']:
        if x['classification']!='unique':continue
        b=(ROOT/x['source']).read_bytes();key=(x['path'],hashlib.sha256(norm(b)).hexdigest())
        if key in seen:continue
        seen[key]='historical/'+name;tree[x['path']]=b
        provenance.append({'path':x['path'],'source':x['source'],'sha256':x['sha256'],'source_mtime':datetime.datetime.fromtimestamp((ROOT/x['source']).stat().st_mtime,datetime.timezone.utc).isoformat()})
    if not tree:continue
    info={'branch':'historical/'+name,'description':description,'source_tree':entry['tree'],'nearest_svn_revision':base,'nearest_revision_is_estimate':True,'files':provenance,'date_policy':'Commit date is migration date, original working-copy commit date and author are unknown. File mtimes are provenance only.','layout':'Orphan branch containing ONLY novel files; missing files do not mean historical deletions.'}
    tree['HISTORICAL-SNAPSHOT.json']=(json.dumps(info,ensure_ascii=False,indent=2)+'\n').encode()
    m=commit(info['branch'],tree,description.encode(),STAMP,'Archive migration <migration@authors.invalid>')
    info['mark']=m;branch_info.append(info)
raw('done\n')
git('fast-import','--quiet','--export-marks='+str(TARGET/'.git/import-marks'),input=out.getvalue())
ids={int(line.split()[0][1:]):line.split()[1] for line in (TARGET/'.git/import-marks').read_text().splitlines()}
for e in analysis:
    for x in e['files']:
        if x['classification']=='unique':
            key=(x['path'],hashlib.sha256(norm((ROOT/x['source']).read_bytes())).hexdigest());x['preserved_in']=seen.get(key)
            assert x['preserved_in'],x
for b in branch_info:b['commit']=ids[b.pop('mark')]
# Retain original SVN revprops (including fractional seconds) separately from unmodified commit messages.
report={'source_revision_zero_properties':revs[0],'source_dump_sha256':hashlib.sha256((ROOT/'google-code/repository.svndump.gz').read_bytes()).hexdigest(),'revisions':[{'svn_revision':r,'git_commit':ids[marks[r]],'properties':revs[r]} for r in range(1,130)],'branches':branch_info,'local_trees':analysis}
(TARGET/'migration').mkdir()
for f in HERE.glob('*.py'):(TARGET/'migration'/f.name).write_bytes(f.read_bytes())
(TARGET/'migration/import-report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
# Preserve node-property history without binary/source archive payloads.
prop_history=[];r=0
for h,b in records():
    if 'Revision-number' in h:r=int(h['Revision-number'])
    elif 'Node-path' in h and int(h.get('Prop-content-length',0)):
        prop_history.append({'revision':r,'path':h['Node-path'],'properties':props(b[:int(h['Prop-content-length'])])})
(TARGET/'migration/svn-node-properties.json').write_text(json.dumps(prop_history,ensure_ascii=False,indent=2)+'\n')
# Materialize official trunk/wiki without touching the migration files.
git('reset','--mixed','main');git('checkout-index','-a','-f')
print('Imported revisions:',129,'historical branches:',len(branch_info))
print('Target:',TARGET)
