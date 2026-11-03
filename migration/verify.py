"""Verify imported Git objects against the original local SVN dump and snapshots."""
import subprocess,sys,datetime,zipfile,io,collections
from analyze import *
TARGET=Path(sys.argv[1] if len(sys.argv)>1 else '/home/andrey/src/iivgrpg')
def git(*args):return subprocess.check_output(['git','-C',str(TARGET),*args])
def oid(b):return hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()
def tree(ref):
    result={}
    for row in git('ls-tree','-rz',ref).split(b'\0'):
        if not row:continue
        meta,p=row.split(b'\t');mode,kind,sha=meta.split();assert kind==b'blob'
        result[p.decode()]=(mode.decode(),sha.decode())
    return result
hist,revs,nodes=load();report=json.loads((TARGET/'migration/import-report.json').read_text())
assert hashlib.sha256((ROOT/'google-code/repository.svndump.gz').read_bytes()).hexdigest()==report['source_dump_sha256']
previous=None;all_paths=set();all_blobs=set();svn_blobs=set();archive_files=0
for entry in report['revisions']:
    r=entry['svn_revision'];ref=entry['git_commit'];expected=mapped(hist[r]);actual=tree(ref)
    assert actual=={p:('100644',oid(b)) for p,b in expected.items()},r
    raw=git('cat-file','commit',ref);header,msg=raw.split(b'\n\n',1)
    assert msg==revs[r]['svn:log'].encode(),('message',r)
    headers=header.splitlines();parents=[x.split()[1].decode() for x in headers if x.startswith(b'parent ')]
    assert parents==([previous] if previous else []),(r,parents)
    stamp=int(datetime.datetime.fromisoformat(revs[r]['svn:date'].replace('Z','+00:00')).timestamp())
    author=revs[r].get('svn:author',revs[r].get('google:author','unknown'))
    for field in [b'author ',b'committer ']:
        h=next(x for x in headers if x.startswith(field));assert int(h.split()[-2])==stamp and h.split()[-1]==b'+0000'
        assert ('<'+(author if '@' in author else author+'@authors.invalid')+'>').encode() in h
    previous=ref;all_paths.update(actual);all_blobs.update(v[1] for v in actual.values());svn_blobs.update(v[1] for v in actual.values())
assert len(report['revisions'])==129
assert git('tag','-l')==b''
for branch in report['branches']:
    ref=branch['branch'];actual=tree(ref)
    assert git('rev-list','--count',ref).strip()==b'1',ref
    assert set(actual)=={x['path'] for x in branch['files']}|{'HISTORICAL-SNAPSHOT.json'}
    for f in branch['files']:
        b=(ROOT/f['source']).read_bytes();assert hashlib.sha256(b).hexdigest()==f['sha256'];assert actual[f['path']][1]==oid(b)
    all_paths.update(actual);all_blobs.update(v[1] for v in actual.values())
# Independently compare the last revision with Google Code's extracted official ZIP.
official=ROOT/'google-code/source/iivgprg';snapshot={}
for directory in ['trunk','wiki']:
    for f in (official/directory).rglob('*'):
        if not f.is_file() or '.svn' in f.relative_to(official).parts:continue
        p=f.relative_to(official).as_posix();p=p[6:] if p.startswith('trunk/') else p
        if not excluded(p):snapshot[p]=norm(f.read_bytes())
assert snapshot=={p:norm(b) for p,b in mapped(hist[129]).items()}
# Working-copy metadata must not hide additional source versions.
known={norm(b) for t in hist.values() for b in t.values()}
for f in ROOT.rglob('*'):
    if not f.is_file() or 'google-code' in f.parts:continue
    if ('text-base' in f.parts and f.name.endswith('.svn-base')) or f.name.endswith('.netbeans-base'):
        b=f.read_bytes()
        if b and not excluded(f.name.removesuffix('.svn-base').removesuffix('.netbeans-base')):
            assert norm(b) in known, str(f)
        archive_files+=1
for local in report['local_trees']:
    for f in local['files']:
        b=(ROOT/f['source']).read_bytes();assert hashlib.sha256(b).hexdigest()==f['sha256']
        if f['classification']=='unique':assert f.get('preserved_in')
# Scan EVERY reachable object, including historical blobs, for disguised dumps or game graphics.
objects=git('rev-list','--objects','--all').splitlines();forbidden=set()
for t in hist.values():
    for p,b in t.items():
        if excluded(p[6:] if p.startswith('trunk/') else p):forbidden.add(oid(b))
reachable={line.split()[0].decode() for line in objects};assert not reachable & forbidden
blob_count=0;jar_count=0
for line in git('cat-file','--batch-all-objects','--batch-check=%(objectname) %(objecttype)').splitlines():
    sha,kind=line.split()
    if kind!=b'blob':continue
    blob_count+=1;b=git('cat-file','blob',sha.decode())
    assert not re.search(rb'(?im)^-- (MySQL dump|phpMyAdmin SQL Dump)',b) and not re.search(rb'(?im)^INSERT INTO `accounts`',b),sha
    if b.startswith(b'PK\x03\x04'):
        jar_count+=1
        with zipfile.ZipFile(io.BytesIO(b)) as z:
            assert not any(Path(x).suffix.lower() in {'.sql','.gz','.zip'} for x in z.namelist()),sha
            assert not any(Path(x).name in {'peasant.png','peasant_selected.png','player.png','grass.png','4_0.png'} for x in z.namelist()),sha
assert not any(excluded(p) for p in all_paths)
fsck=subprocess.run(['git','-C',str(TARGET),'fsck','--full','--strict'],capture_output=True,text=True)
assert fsck.returncode==0,fsck.stdout+fsck.stderr
result={'svn_commits_verified':129,'orphan_branches_verified':len(report['branches']),'local_trees_verified':len(report['local_trees']),'metadata_source_files_checked':archive_files,'git_blobs_scanned':blob_count,'jar_blobs_scanned':jar_count,'official_final_snapshot_matches':True,'dates_authors_messages_trees_match':True,'no_svn_tags':True,'no_excluded_blobs':True,'git_fsck':'passed'}
print(json.dumps(result,ensure_ascii=False,indent=2))
