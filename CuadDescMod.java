/*lectura:V Ev1,1 v1,2...vE,1 vE,2indices 0 indexados.*/
//probar refinamiento.
//que pasa si el forcing es vacio?(k4)
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
public class CuadDescMod{
public static int V;//numero de nodos
public static int E;//numero de aristas
public static HashMap<Integer,HashSet<Integer>> ady;//HashMap es diccionario,Vector es lista enlazada.
public static int pad[];//bosque de componentes conexas(union-find)
public static int compPert[];//compPert[n]=i significa n esta en la componente de i
public static HashMap<Integer,Vector<Integer>> componentes;
public static Vector<Integer> indComp;
public static HashMap<Integer,HashSet<Integer>> adyForG;//forcing graph
public static void main(String args[]) throws Exception{
BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
String t[] = lector.readLine().split(" ");
V = Integer.parseInt(t[0]);
E = Integer.parseInt(t[1]);
ady = new HashMap<Integer,HashSet<Integer>>();
pad = new int[V];
compPert = new int[V];
indComp = new Vector<Integer>();
componentes = new HashMap<Integer,Vector<Integer>>();
for(int n = 0;n<V;n++)pad[n]=n;
for(int n = 0;n<E;n++){//leyendo y creando lista adyacencia.
t = lector.readLine().split(" ");
int a = Integer.parseInt(t[0]);
int b = Integer.parseInt(t[1]);
if(a<0 || a>=V || b<0 || b>=V)throw new Exception("nodos son 0-indexados y menores a "+V);
if(!ady.keySet().contains(a))
ady.put(a,new HashSet<Integer>());
if(!ady.keySet().contains(b))
ady.put(b,new HashSet<Integer>());
ady.get(a).add(b);
ady.get(b).add(a);
union(a,b);//a y b estan en la misma componente conexa
}
for(int n = 0;n<V;n++){
compPert[n]=find(n);
if(!componentes.keySet().contains(compPert[n]))
componentes.put(compPert[n],new Vector<Integer>());
componentes.get(compPert[n]).add(n);
if(compPert[n]==n)indComp.add(n);
}
System.out.println(componentes);
arbol res;
if(indComp.size()>1){
res = new arbol(0);
for(int n = 0;n<indComp.size();n++){
res.raiz.hijos.add(DescMod(componentes.get(indComp.get(n))).raiz);
}
}else
res = DescMod(componentes.get(indComp.get(0)));
System.out.println(res);
}
public static arbol DescMod(Vector<Integer> dom){
arbol res = new arbol(-1);
HashSet<Integer> dominio = new HashSet<Integer>();
for(int n = 0;n<dom.size();n++)dominio.add(dom.get(n));
if(dom.size()==0)return res;
int pivot = dom.get(dom.size()-1);
dominio.remove(dom.get(dom.size()-1));
dom.remove(dom.size()-1);
part Mgv = new part(dom,dominio,pivot);
System.out.println("Creando Mgv="+Mgv);
//crea cociente
HashMap<Integer,HashSet<Integer>> adyGPrima = new HashMap<Integer,HashSet<Integer>>();
int pivotCociente = -1;
for(int p:Mgv.L.keySet()){
for(int q:Mgv.L.keySet()){
int an = Mgv.L.get(p).repre;//uso que son modulos, cualquiera con cualquiera
int am = Mgv.L.get(q).repre;
if(Mgv.L.get(p).pivot)pivotCociente=an;
if(an==am)continue;
if(!ady.get(an).contains(am))continue;
if(!adyGPrima.keySet().contains(an))
adyGPrima.put(an,new HashSet<Integer>());
adyGPrima.get(an).add(am);
}
}
System.out.println(pivotCociente+" "+adyGPrima);
//crea el forcing
adyForG = new HashMap<Integer,HashSet<Integer>>();
for(int vFor:adyGPrima.keySet()){
for(int wFor:adyGPrima.keySet()){
if(vFor==wFor || vFor==pivotCociente || wFor==pivotCociente)continue;
if(adyGPrima.get(vFor).contains(pivotCociente) ^
adyGPrima.get(vFor).contains(wFor)){
if(!adyForG.keySet().contains(vFor))
adyForG.put(vFor,new HashSet<Integer>());
adyForG.get(vFor).add(wFor);
}
}
}
System.out.println(adyForG);
//Mando tarjan
HashMap<Integer,HashSet<Integer>> SCC = Tarjan();
System.out.println("-->"+SCC);
nodo u = res.raiz;
while(true){
//aca deberia ir algo pero no porque es para ver si git funca
nodo w = new nodo(-1);
u.hijos.add(w);
//getLeaf(GG)..estructura de padres
Vector<Vector<Integer>> F = new Vector<Vector<Integer>>();
//un treeset como cola de prioridad y le quito outdegree
//necesitaria el grafo con las flechas pal otro lado
//F=//quien es Leaf en Componente de Mgv
u.clase = F.size()>1?1:2;
if(F.size()==0)break;
for(int n = 0;n<F.size();n++){
arbol gx = DescMod(F.get(n));
if(u.clase==2 && gx.raiz.clase==2){
for(int m= 0;m<gx.raiz.hijos.size();m++)
u.hijos.add(gx.raiz.hijos.get(n));
}else
u.hijos.add(gx.raiz);
}
u = w;
}
return res;
}
public static int desc;
public static HashMap<Integer,Integer> lowlink;
public static HashMap<Integer,Integer> index;
public static HashSet<Integer> vis;
public static HashSet<Integer> visScc;
public static Stack<Integer> sccS;
public static Vector<Integer> comp;
public static HashMap<Integer,Vector<Integer>> compp;
public static HashMap<Integer,HashSet<Integer>> Tarjan(){
comp = new Vector<Integer>();
lowlink = new HashMap<Integer,Integer>();
index = new HashMap<Integer,Integer>();
vis = new HashSet<Integer>();
visScc = new HashSet<Integer>();
sccS = new Stack<Integer>();
compp = new HashMap<Integer,Vector<Integer>>();	
HashMap<Integer,HashSet<Integer>> res = new HashMap<Integer,HashSet<Integer>>();
desc=0;
for(int a:adyForG.keySet())
if(!vis.contains(a))
scc(a);
for(int n=0;n<comp.size();n++)
res.put(comp.get(n),new HashSet<Integer>());
for(int n = 0;n<comp.size();n++)
for(int m = 0;m<comp.size();m++)
if(n!=m){
if(adyForG.get(comp.get(n)).contains(comp.get(m)))
res.get(comp.get(n)).add(comp.get(m));
}
//System.out.println(lowlink+" "+index+" "+vis);
return res;
}
public static void scc(int a){
if(!lowlink.keySet().contains(a))
lowlink.put(a,desc);
index.put(a,desc++);
sccS.push(a);
vis.add(a);
for(int h:adyForG.get(a)){
if(!lowlink.keySet().contains(h))
scc(h);
if(vis.contains(h))
lowlink.put(a,Math.min(lowlink.get(a),lowlink.get(h)));
}
if(lowlink.get(a)==index.get(a)){
int puntero = -1;
//System.out.println(a+" "+sccS+" "+lowlink);
Vector<Integer> compa = new Vector<Integer>();
while(!sccS.isEmpty()){
puntero = sccS.pop();
visScc.add(puntero);
compa.add(puntero);
vis.remove(puntero);
if(puntero==a)break;
}
comp.add(a);
compp.put(a,compa);
}

}
public static int find(int a){
while(a!=pad[a])
a = pad[a];
return a;
}
public static void union(int a,int b){//path compression puede ser usado
int pa = find(a);
int pb = find(b);
pad[pa]=pb;
}
}
class part{//a partition of a set
public bloque ini;
public int ha = 0;
public HashMap<Integer,bloque> L = new HashMap<Integer,bloque>();
public HashMap<Integer,HashSet<Integer>> out = new HashMap<Integer,HashSet<Integer>>();
public TreeSet<Integer> outStack = new TreeSet<Integer>();
//public HashMap<Integer,Veoctor<Integer>> ady;
public part(Vector<Integer> dom,HashSet<Integer> dominio,int pivot){
HashSet<Integer> singleV = new HashSet<Integer>();
singleV.add(pivot);
ini = new bloque(ha++,dom.get(0),dominio,singleV);
L.put(ini.hash,ini);
out.put(pivot,new HashSet<Integer>());out.get(pivot).add(0);
outStack.add(pivot);
//Iterator it = out.keySet().iterator();//iterator over outsiders
while(!outStack.isEmpty()){//while(!out.keySet().isEmpty()){
//System.out.println("Refi: L="+L+" outsiders="+out+" outS="+outStack);
//System.out.println("Previa next: "+out);
int w = outStack.pollFirst();//(Integer)it.next();//use iterator
if(out.get(w).size()>1)outStack.add(w);
Iterator itt = out.get(w).iterator();
int claseApuntada = -1;
boolean encontroClase = false;
while(itt.hasNext()){
claseApuntada= (Integer)itt.next();
if(L.keySet().contains(claseApuntada)&& (encontroClase=true))break;
}
if(!encontroClase){
out.remove(w);//it.remove();
outStack.remove(w);
continue;
}
bloque S = L.get(claseApuntada);
int hS = S.hash;
HashSet<Integer> izq = new HashSet<Integer>();
HashSet<Integer> der = new HashSet<Integer>();
int repreIzq = -1;
int repreDer = -1;
for(int e: S.elementos){
if(CuadDescMod.ady.get(w).contains(e)){der.add(e);repreDer=e;
}else{ izq.add(e);repreIzq = e;}
}
//refinamiento de S en iz,der
if(izq.size()>0 && der.size()>0){
L.remove(hS);
HashSet<Integer> izO = new HashSet<Integer>();
HashSet<Integer> derO = new HashSet<Integer>();
for(int outS:S.outsiders){
if(outS==w)continue;
izO.add(outS);
derO.add(outS);
if(!out.keySet().contains(outS))out.put(outS,new HashSet<Integer>());
out.get(outS).add(ha);
out.get(outS).add(ha+1);
out.get(outS).remove(hS);
outStack.add(outS);
}
for(int outDer:der){
if(outDer==w)continue;
izO.add(outDer);
if(!out.keySet().contains(outDer))
out.put(outDer,new HashSet<Integer>());
if(!outStack.contains(outDer))outStack.add(outDer);
out.get(outDer).add(ha);
}
//izO.addAll(der);
//izO.remove(w);
L.put(ha,new bloque(ha,repreIzq,izq,izO));
ha++;
for(int outDer:izq){
if(outDer==w)continue;
derO.add(outDer);
if(!out.keySet().contains(outDer))
out.put(outDer,new HashSet<Integer>());
if(!outStack.contains(outDer))outStack.add(outDer);
out.get(outDer).add(ha);

}
//derO.addAll(izq);
//derO.remove(w);
L.put(ha,new bloque(ha,repreDer,der,derO));
ha++;
out.get(w).remove(hS);
}else{
out.get(w).remove(hS);
L.get(hS).outsiders.remove(w);
}
if(out.get(w).size()==0){
out.remove(w);//it.remove();
outStack.remove(w);
}
//System.out.println("Refi(w="+w+" "+hS+"): L="+L+" outsiders="+out+" outS="+outStack);
}
L.put(ha,new bloque(ha,pivot,singleV,null));
L.get(ha).pivot=true;
}
public String toString(){
return "L="+L+" out="+out;
}
}

class bloque{//conforman part
public boolean pivot=false;
public int hash;
public int repre;//repre \in elementos.
public HashSet<Integer> outsiders;
public HashSet<Integer> elementos;
public int iz,der;
public bloque(int hash,int repre,HashSet<Integer> elementos,HashSet<Integer> outsiders){
this.repre = repre;
this.hash = hash;
this.outsiders = outsiders;
this.elementos = elementos;
}
public String toString(){
return repre+" "+elementos+" "+outsiders+" "+iz+" "+der;
}
}
class nodo{
public Vector<nodo> hijos;
public int clase;
public nodo(int clase){
this.clase = clase;
hijos = new Vector<nodo>();
}
public String toString(){
return clase+" "+hijos;
}
}

class arbol{
public nodo raiz;
public arbol(int a){
raiz = new nodo(a);
}
public String toString(){
return ""+raiz;
}
}
