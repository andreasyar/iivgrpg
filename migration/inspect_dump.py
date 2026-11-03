import gzip,hashlib,json
from pathlib import Path
ROOT=Path('/home/andrey/src/iivgrpg-legacy')
def records():
    with gzip.open(ROOT/'google-code/repository.svndump.gz','rb') as f:
        while True:
            line=f.readline()
            if not line: break
            if not line.strip(): continue
            h={}
            while line.strip():
                k,v=line.rstrip(b'\n').split(b': ',1); h[k.decode()]=v.decode();line=f.readline()
            yield h,f.read(int(h.get('Content-length',0)))
def props(b):
    p={};i=0
    while i<len(b):
        j=b.index(b'\n',i); line=b[i:j];i=j+1
        if line==b'PROPS-END':break
        op,n=line.split();n=int(n);k=b[i:i+n].decode();i+=n+1
        if op==b'D':p[k]=None;continue
        j=b.index(b'\n',i);v,n=b[i:j].split();i=j+1;n=int(n);p[k]=b[i:i+n].decode();i+=n+1
    return p
def load():
    trees={0:{}}; revs={};nodes=[];cur=0;tree={};properties={}
    for h,b in records():
        if 'Revision-number' in h:
            if cur: trees[cur]=tree.copy()
            cur=int(h['Revision-number']);revs[cur]=props(b);continue
        if 'Node-path' not in h:continue
        nodes.append((cur,h));path=h['Node-path'];act=h['Node-action']
        if act in ('delete','replace'):
            for p in list(tree):
                if p==path or p.startswith(path+'/'):del tree[p]
        if act=='delete':continue
        if 'Node-copyfrom-path' in h:
            old=h['Node-copyfrom-path'];src=trees[int(h['Node-copyfrom-rev'])]
            for p,v in src.items():
                if p==old or p.startswith(old+'/'):tree[path+p[len(old):]]=v
        if h.get('Node-kind')=='file' or path in tree:
            n=int(h.get('Prop-content-length',0))
            if 'Text-content-length' in h:
                data=b[n:];assert len(data)==int(h['Text-content-length'])
                for key,func in [('Text-content-md5',hashlib.md5),('Text-content-sha1',hashlib.sha1)]:
                    if key in h: assert func(data).hexdigest()==h[key],(cur,path)
                tree[path]=data
            elif act=='add' and path not in tree:tree[path]=b''
    trees[cur]=tree.copy()
    return trees,revs,nodes
if __name__=='__main__':
    trees,revs,nodes=load()
    print(f"Verified {len(revs)-1} revisions, {len(nodes)} nodes, {len(trees[129])} final files")
