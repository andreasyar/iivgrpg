from inspect_dump import *
import collections,re

def norm(b):return b.replace(b'\r\n',b'\n')
def excluded(path):
    p=Path(path)
    if p.suffix.lower() in {'.sql','.gz','.zip','.db'}:return 'database/archive'
    if p.suffix.lower() in {'.png','.jpg','.jpeg','.gif','.bmp','.ico','.swf'} and not path.startswith('server/netty/'):return 'game graphics'
    return None

def mapped(tree):
    return {(p[6:] if p.startswith('trunk/') else p):b for p,b in tree.items() if (p.startswith('trunk/') or p.startswith('wiki/')) and not excluded(p[6:] if p.startswith('trunk/') else p)}

def local_files(d):
    files=[]
    if d==ROOT:
        for s in ['client','server']:files.extend((d/s).rglob('*'))
        files += [d/'JavaTestServer.java',d/'readme.txt']
    else:files=list(d.rglob('*'))
    result=[]
    for f in sorted(files):
        if not f.is_file():continue
        rel=f.relative_to(d).as_posix()
        if any(x.startswith('.svn') for x in Path(rel).parts):continue
        if d.name=='client':rel='client/'+rel
        elif d.name in ('iivgrpgserver','iivgrpg-server') and not rel.startswith('client/'):rel='server/'+rel
        if rel.startswith('client/client/'):rel=rel[7:]
        result.append((rel,f))
    return result

def analyze():
    trees,revs,nodes=load();hist={r:mapped(t) for r,t in trees.items()}
    known=collections.defaultdict(list)
    for r,t in hist.items():
        for p,b in t.items():known[hashlib.sha256(norm(b)).hexdigest()].append((r,p))
    out=[]
    for e in json.loads((ROOT/'preserved-source-trees.json').read_text()):
        d=Path(e['destination']);items=[];counts=collections.Counter()
        for p,f in local_files(d):
            b=f.read_bytes();sha=hashlib.sha256(norm(b)).hexdigest()
            why=excluded(p)
            generated=any(x in ('build','dist','bin','private') for x in Path(p).parts) or p.endswith(('.class','.netbeans-base'))
            kind='excluded' if why else 'empty-placeholder' if not b else 'modern-ide-change' if b'__CREATED_BY_JAVA_LANGUAGE_SERVER__' in b else 'generated' if generated else 'in-svn' if sha in known else 'unique'
            counts[kind]+=1
            items.append({'path':p,'source':str(f.relative_to(ROOT)),'sha256':hashlib.sha256(b).hexdigest(),'classification':kind,'reason':why,'matches':known.get(sha,[])[-1:]})
        out.append({'tree':str(d.relative_to(ROOT)),'counts':dict(counts),'files':items})
    Path('/tmp/iivgrpg-analysis.json').write_text(json.dumps(out,ensure_ascii=False,indent=2))
    for e in out:
        print(e['tree'],e['counts'])
    return hist,revs,out,known
if __name__=='__main__':analyze()
