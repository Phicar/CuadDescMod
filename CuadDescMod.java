/*lectura:V Ev1,1 v1,2...vE,1 vE,2indices 0 indexados.*/
//probar refinamiento.
//que pasa si el forcing es vacio?(k4)
//lo que no entiendo tiene label "porver"
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
public class CuadDescMod{
	//Tabs de la impresion
	public static boolean impresionTodo = true;
	public static boolean impresionArbol = true;
	public static int V;//numero de nodos
	public static int E;//numero de aristas
	public static HashMap<Integer,HashSet<Integer>> ady;//HashMap es diccionario,Vector es lista enlazada.
	public static int pad[];//bosque de componentes conexas(union-find)
	public static int compPert[];//compPert[n]=i significa n esta en la componente de i
	public static HashMap<Integer,Vector<Integer>> componentes;//mapea componente i a {n\in V: compPert[n]=i}
	public static Vector<Integer> indComp;//determina el representante(padre en el arbol) de la componente
	public static HashMap<Integer,HashSet<Integer>> adyForG;//forcing graph 
	public static HashMap<Integer,nodo> ptf = new HashMap<Integer,nodo>();//arbol final
	public static int hashAct = 0;
	public static void main(String args[]) throws Exception{
		BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
		String t[] = lector.readLine().split(" ");
		//lectura, en consola "vertices edges"
		V = Integer.parseInt(t[0]);
		E = Integer.parseInt(t[1]);
		ady = new HashMap<Integer,HashSet<Integer>>();
		pad = new int[V];
		compPert = new int[V];
		indComp = new Vector<Integer>();
		componentes = new HashMap<Integer,Vector<Integer>>();
		/*Inicializacion de conjunto disjunto para tratar componentes conexas, al comienzo el grafo es nulo.*/
		for(int n = 0;n<V;n++)pad[n]=n;
		/*Se recorren las aristas de la entrada ej.
		3 3
		0 1
		1 2
		0 2
		representa la entrada del grafo K_3
		*/
		for(int n = 0;n<E;n++){
			t = lector.readLine().split(" ");
			int a = Integer.parseInt(t[0]);
			int b = Integer.parseInt(t[1]);
			if(a<0 || a>=V || b<0 || b>=V)//los nodos deben ser 0 indexados y no debe haber saltos en los vertices
				throw new Exception("nodos son 0-indexados y menores a "+V);
			/*Se crea el conjunto de vecinos para el nodo a*/
			if(!ady.keySet().contains(a))
				ady.put(a,new HashSet<Integer>());
			/*Se crea el conjunto de vecinos para el nodo a*/
			if(!ady.keySet().contains(b))
				ady.put(b,new HashSet<Integer>());
			/*Se relacionan los vertices en la estructura de lista y en el conjunto disjunto*/
			ady.get(a).add(b);
			ady.get(b).add(a);//DESCOMENTAR
			union(a,b);//a y b estan en la misma componente conexa
		}
		/*Se construye el mapeo entre componente y nodo y viceversa*/
		for(int n = 0;n<V;n++){
			compPert[n]=find(n);
			if(!componentes.keySet().contains(compPert[n]))
			componentes.put(compPert[n],new Vector<Integer>());
			componentes.get(compPert[n]).add(n);
			if(compPert[n]==n)indComp.add(n);//si n==find(n), n es representante de componente
		}
		if(impresionTodo)
		System.out.println(componentes);
		/*Donde deberia quedar el arbol de factorizacion*/
		//si hay mas de una componente se recorre componente a componente
		if(indComp.size()>1){
			ptf.put(0,new nodo(0,null));
			hashAct++;
				for(int n = 0;n<indComp.size();n++){
					int hasho = DescMod(componentes.get(indComp.get(n)),"");
					ptf.get(0).hijos.add(hasho);
				}
		}else{ //solo una componente en G
			DescMod(componentes.get(indComp.get(0)),"");
		}
		/*Ajustar colores*/
		colorear(0);
		//recortar(0,-1);
		/*Se imprime el arbol*/
		if(impresionArbol)
		System.out.println("Arbol final:"+ptf.get(0));
	}
	public static void recortar(int a,int b){
		HashSet<Integer> h = (HashSet<Integer>)ptf.get(a).hijos.clone();
		int ac = ptf.get(a).clase;
		if(h.isEmpty())return;
		if(b==-1)
			for(int n:h)recortar(n,a);
		else{
			int bc = ptf.get(b).clase;
			if(ac==bc && ac%2==0){
				ptf.get(b).hijos.remove(a);
				for(int n:h)ptf.get(b).hijos.add(n);
				recortar(b,-1);
			}else{
				for(int n:h)recortar(n,a);
			}
		}
	}
	public static void colorear(int a){
		HashSet<Integer> h = ptf.get(a).hijos;
		if(h.isEmpty())return;
		int r = -1;
		boolean completo = true;
		for(int n:h){
			HashSet<Integer> repres = ptf.get(n).et;
			if(r==-1)r = (int)repres.iterator().next();
			else{
			if(!ady.get(r).contains((int)repres.iterator().next()))completo = false;
			}
			colorear(n);
			
		}
		if(ptf.get(a).clase==2 && !completo)ptf.get(a).clase = 0;
	
	}
	/*La funcion ptf(G) en el paper. 
	Recibe el conjunto dominio que es, basicamente, los vertices de un grafo conexo.
	Devuelve el arbol de descomposicion
	No esta terminada:
		falta saltar en el arbol.
	*/
	public static int DescMod(Vector<Integer> dom,String nivel){
		/*se crea el arbol, con nodo en -1*/
		
		/*Se crea un conjunto del dominio, busqueda en O(1)*/
		HashSet<Integer> dominio = new HashSet<Integer>();
		for(int n = 0;n<dom.size();n++)
			dominio.add(dom.get(n));
		arbol res = new arbol(-1,dominio);
		/*Si el grafo es vacio, no hay nada que hacer*/
		if(dom.size()==0)
			return -1;
		if(dom.size()==1){
			int mira = hashAct;
			ptf.put(hashAct++,new nodo(3,dominio));
			return mira;
		}
		/*Se escoje de pivot el ultimo vertice en el dominio
		por ver: Hay cambio dependiendo del vertice pivot que se escoja?
		*/
		int pivot = dom.get(dom.size()-1);
		dominio.remove(dom.get(dom.size()-1)); 
		dom.remove(dom.size()-1);
		/*llama M(g,v) el algoritmo 3.1 en el paper para crear los clanes respectivos al pivot*/
		part Mgv = new part(dom,dominio,pivot);
		if(impresionTodo){
		System.out.println(nivel+"Creando Mgv="+Mgv);
		System.out.println(nivel+"Creando Mgv pivot = "+pivot+" dominio="+dominio+" "+dom);
		System.out.println(ady);
		}
		/*se creara g' el grafo cociente del grafo original g y la particion creada en Mgv*/
		HashMap<Integer,HashSet<Integer>> adyGPrima = new HashMap<Integer,HashSet<Integer>>();
		int pivotCociente = -1;
		/*Itera sobre L de la particion en clase*/
		for(int p:Mgv.L.keySet()){
			for(int q:Mgv.L.keySet()){
				int an = Mgv.L.get(p).repre;//uso que son modulos, cualquiera con cualquiera
				int am = Mgv.L.get(q).repre;
				if(Mgv.L.get(p).pivot)pivotCociente=an;
				if(an==am)continue;
				if(!adyGPrima.keySet().contains(an))
				adyGPrima.put(an,new HashSet<Integer>());
				if(!ady.get(an).contains(am))continue;
				adyGPrima.get(an).add(am);
			}
		}
		if(impresionTodo)
		System.out.println(nivel+"pivotCociente: "+pivotCociente+" grafo cociente: "+adyGPrima);
		/*
		Crea el forcing G(g',v') donde g' es el grafo cociente y v' es la imagen de v(el pivot) bajo el mapeo cociente.
		(vFor,wFor)\in E_{G(g',v')} sii vFor distingue a wFor y el pivot. 
		x distingue a y de z si (x,y) y (x,z) tienen colores diferentes.
		porver
		*/
		adyForG = new HashMap<Integer,HashSet<Integer>>();//DESCOMENTAR
		for(int vFor:adyGPrima.keySet()){
			if(!adyForG.keySet().contains(vFor))
				adyForG.put(vFor,new HashSet<Integer>());
			for(int wFor:adyGPrima.keySet()){
				if(vFor==wFor || vFor==pivotCociente || wFor==pivotCociente)continue;
				if(adyGPrima.get(vFor).contains(pivotCociente) ^
				adyGPrima.get(vFor).contains(wFor))
					adyForG.get(vFor).add(wFor);
			}
		}//DESCOMENTAR
		if(impresionTodo)
		System.out.println(nivel+"Forcing: "+adyForG);
		/*Se encuentra el grafo componente G''*/
		Vector<HashMap<Integer,HashSet<Integer>>> SCC = Tarjan();//grafo componente,grafo invertido,componentes
		if(impresionTodo)
			System.out.println(nivel+"Component Graph: "+SCC.get(0)+" "+comp+" "+SCC.get(1)+" "+SCC.get(2));
		HashSet<Integer> vProcesado = new HashSet<Integer>();
		HashSet<Integer> vPila = new HashSet<Integer>();
		int miraiz = hashAct++;
		ptf.put(miraiz,new nodo(2,dominio));//cambiar esto no a dominio sino a MVG
		int actual = miraiz;//u
		int nextAct=0;//w
		Stack<Integer> hojas = new Stack<Integer>();
		for(int n:SCC.get(0).keySet())
			if(SCC.get(0).get(n).isEmpty()){
				if(n==pivot)continue;//PUT THE PIVOR OR NOT TO PUT IT
				hojas.push(n);
				vPila.add(n);
			}
		if(impresionArbol)
			System.out.println(nivel+"leaf "+hojas);
		/*Mientras G'' no sea vacio*/
		boolean entra = false;
		while(!hojas.isEmpty()){
			int repreLeaf = hojas.pop();
			vProcesado.add(repreLeaf);
			vPila.remove(repreLeaf);
			HashSet<Integer> grafoComp = SCC.get(2).get(repreLeaf);
			if(grafoComp==null){
				grafoComp = new HashSet<Integer>();
				grafoComp.add(repreLeaf);
			}
			//2 lines bullshit
			//nodo w = new nodo(-1,grafoComp);
			//u.hijos.add(w);
			//getLeaf(GG)..estructura de padres
			/*En el algoritmo aca es donde quitamos un nodo sumidero del DAG G''*/
			if(entra){
				actual = nextAct;//u = u.hijos.get(u.hijos.size()-1);
				ptf.get(actual).et= grafoComp;
			}
			if(impresionArbol)
				System.out.println(nivel+"DentroWhile "+repreLeaf+" "+grafoComp);
			for(int pad: SCC.get(1).get(repreLeaf)){
				if(vProcesado.contains(pad) || vPila.contains(pad))continue;
				SCC.get(0).get(pad).remove(repreLeaf);
				if(SCC.get(0).get(pad).isEmpty()){
					hojas.push(pad);
					vPila.add(pad);
				}
			}
			//System.out.println(hojas);
			//un treeset como cola de prioridad y le quito outdegree
			//necesitaria el grafo con las flechas pal otro lado
			//F=//quien es Leaf en Componente de Mgv
			/*se le asocia el tipo de nodo 1 primitivo,2 completo*/
			ptf.get(actual).clase = grafoComp.size()>1?1:2;
			/*G'' quedo vacio*/
			HashSet<Integer> todosNodos = new HashSet<Integer>();
			for(int n:grafoComp){
				HashSet<Integer> toditos = new HashSet<Integer>();
				Vector<Integer> F = new Vector<Integer>();
				for(int m:Mgv.L.get(Mgv.repreBlo.get(n)).elementos){
					F.add(m);
					toditos.add(m);
					todosNodos.add(m);
				}
				if(F.size()>0){
					if(impresionArbol)
					System.out.println(nivel+"Manda recursivamente "+F);
					/*recursion*/
					if(impresionArbol)
						System.out.println(nivel+"antes->"+actual+"-->"+ptf.get(actual));
					int abajo = DescMod(F,nivel+"\t");//arbol gx = DescMod(F,nivel+"\t");
					ptf.get(abajo).et = toditos;
					if(impresionArbol)
						System.out.println(nivel+"Devuelve "+ptf.get(abajo));
					/*Establecer si es vacio o completo (color)*/
					/*if(ptf.get(actual).clase==2){
						if(F.size()==1 || !ady.get(F.get(0)).contains(F.get(1)))
							ptf.get(actual).clase = 0;
					}*/
					if(ptf.get(actual).clase%2==15 && ptf.get(actual).clase==ptf.get(abajo).clase){//ojo 15
						/*si la raiz del recursivo y u son completos se pegan los hijos, asi que se recorren*/
						for(int m:ptf.get(abajo).hijos)
							ptf.get(actual).hijos.add(m);
						//System.out.println(actual+" PIERDE "+abajo);
						ptf.get(actual).hijos.remove(abajo);
					}else{ // solo se pega la raiz a u.
						//System.out.println("Le acabo de agregar a "+ptf.get(actual)+" "+ptf.get(abajo));
						ptf.get(actual).hijos.add(abajo);
					}
					if(impresionArbol)
					System.out.println(nivel+"despues->"+actual+"-->"+ptf.get(actual));
				}
			}
			if(impresionArbol)
			System.out.println(nivel+actual+" TODOS "+todosNodos);
		if(!hojas.isEmpty()){
			
			ptf.put(hashAct,new nodo(-1,null));
			ptf.get(actual).hijos.add(hashAct);
			if(entra)
				ptf.get(actual).et = todosNodos;
			nextAct = hashAct;//hashAct++;//u.hijos.add(new nodo(-1,null));
			hashAct++;
			entra = true;
		}
			
		}
		HashSet<Integer> soloPivot = new HashSet<Integer>();
		soloPivot.add(pivot);
		ptf.put(hashAct,new nodo(3,soloPivot));
		ptf.get(actual).hijos.add(hashAct++);
		System.out.println(nivel+"APUNTADOR "+miraiz);
		return miraiz;
	}
	public static int desc;
	public static HashMap<Integer,Integer> lowlink;
	public static HashMap<Integer,Integer> index;
	public static HashSet<Integer> vis;
	public static HashSet<Integer> visScc;
	public static Stack<Integer> sccS;
	public static HashMap<Integer,Integer> vRepre;//v mapea a su representante
	public static Vector<Integer> comp;//los vertices del component graph
	public static HashMap<Integer,HashSet<Integer>> compp;//Particion tarjan: representante-->conjunto
	/*Devuelve el digrafo de las componentes fuertemente conexas (component graph en el paper)*/
	public static Vector<HashMap<Integer,HashSet<Integer>>> Tarjan(){
		comp = new Vector<Integer>();
		lowlink = new HashMap<Integer,Integer>();
		index = new HashMap<Integer,Integer>();
		vis = new HashSet<Integer>();
		visScc = new HashSet<Integer>();
		sccS = new Stack<Integer>();
		compp = new HashMap<Integer,HashSet<Integer>>();
		vRepre = new HashMap<Integer,Integer>();
		HashMap<Integer,HashSet<Integer>> res = new HashMap<Integer,HashSet<Integer>>();
		HashMap<Integer,HashSet<Integer>> resRev = new HashMap<Integer,HashSet<Integer>>();
		desc=0;
		/*recorre los vertices de G y manda tarjan*/
		for(int a:adyForG.keySet())
			if(!vis.contains(a))
				scc(a);
		/*Recorre las componentes del Tarjan y crea el digrafo que definen sus componentes*/
		
		for(int n=0;n<comp.size();n++){
			res.put(comp.get(n),new HashSet<Integer>());
			//if(!compp.keySet().contains(comp.get(n)))compp.put(comp.get(n),new HashSet<Integer>());
			resRev.put(comp.get(n),new HashSet<Integer>());
		}
		//Le aporta O(E) a Tarjan
		for(int n: adyForG.keySet()){
			if(!vRepre.keySet().contains(n)){
				vRepre.put(n,comp.size());
				comp.add(n);
				res.put(n,new HashSet<Integer>());
			resRev.put(n,new HashSet<Integer>());
			}
			for(int m: adyForG.get(n)){
				int rn = vRepre.get(n);
				int rm = vRepre.get(m);				
				if(rn==rm)continue;
				res.get(comp.get(rn)).add(comp.get(rm));
				resRev.get(comp.get(rm)).add(comp.get(rn));
			}
		}
		if(impresionTodo)
		System.out.println("TARJAN "+compp);
		//System.out.println(lowlink+" "+index+" "+vis);
		Vector<HashMap<Integer,HashSet<Integer>>> ress =new Vector<HashMap<Integer,HashSet<Integer>>>() ;
		ress.add(res);
		ress.add(resRev);
		ress.add(compp);
		return ress;
	}
	/*
	Algoritmo Tarjan para encontrar componentes fuertemente conexas en un grafo dirigido
	O(V+E)
	*/
	public static void scc(int a){
		if(!lowlink.keySet().contains(a))
			lowlink.put(a,desc);
		index.put(a,desc++);
		sccS.push(a);
		vis.add(a);
		if(adyForG.keySet().contains(a)){
		for(int h:adyForG.get(a)){
			if(!lowlink.keySet().contains(h))
				scc(h);
			if(vis.contains(h))
				lowlink.put(a,Math.min(lowlink.get(a),index.get(h)));
		}
		}
		
		if(lowlink.get(a)==index.get(a)){
			int puntero = -1;
			//System.out.println(a+" "+sccS+" "+lowlink);
			HashSet<Integer> compa = new HashSet<Integer>();
			while(!sccS.isEmpty()){
				puntero = sccS.pop();
				visScc.add(puntero);
				compa.add(puntero);
				vRepre.put(puntero,comp.size());
				vis.remove(puntero);
				if(puntero==a)break;
			}
			comp.add(a);
			compp.put(a,compa);
			
		}

	}
	/*dado un vertice a, encuentra el representante de la componente conexa en la que esta a*/
	public static int find(int a){
		while(a!=pad[a])
		a = pad[a];
		return a;
	}
	/*une componentes conexas, asignando un representante comun, cuando detecta una arista entre dos*/
	public static void union(int a,int b){//path compression puede ser usado
		int pa = find(a);
		int pb = find(b);
		pad[pa]=pb;
	}
}
/*Clase particion
Recibe dominio y un pivot
maneja los bloques de la particion
*/
class part{//a partition of a set
	public bloque ini;//mantiene el bloque inicial
	public int ha = 0;//mantiene el identificador de los bloques (hash en clase bloque)
	public HashMap<Integer,bloque> L = new HashMap<Integer,bloque>();//mapea ha a su correspondiente bloque
	public HashMap<Integer,HashSet<Integer>> out = new HashMap<Integer,HashSet<Integer>>();//mapeo entre outsiders y bloques
	public TreeSet<Integer> outStack = new TreeSet<Integer>();//Los outsiders
	public HashMap<Integer,Integer> repreBlo = new HashMap<Integer,Integer>();
	//public HashMap<Integer,Veoctor<Integer>> ady;
	/*Constructor de la particion*/
	public part(Vector<Integer> dom,HashSet<Integer> dominio,int pivot){
		HashSet<Integer> singleV = new HashSet<Integer>();
		singleV.add(pivot);
		/*
		Crea el bloque principal a refinar, estan todos los vertices y se deja como representante del bloque al primer elemento
		El unico outsider es el pivot
		*/
		ini = new bloque(ha,dom.get(0),dominio,singleV);
		repreBlo.put(dom.get(0),ha++);
		L.put(ini.hash,ini);
		out.put(pivot,new HashSet<Integer>());
		out.get(pivot).add(0);
		outStack.add(pivot);
		//Iterator it = out.keySet().iterator();//iterator over outsiders
		/*
		Mientras haya outsiders
		*/
		while(!outStack.isEmpty()){//while(!out.keySet().isEmpty()){
		//System.out.println("Refi: L="+L+" outsiders="+out+" outS="+outStack);
		//System.out.println("Previa next: "+out);
		int w = outStack.pollFirst();//(Integer)it.next();//use iterator
		if(out.get(w).size()>1)//Si el nodo w es outsider de mas de un bloque, seguira siendo outsider
			outStack.add(w);
		Iterator itt = out.get(w).iterator();
		int claseApuntada = -1;
		boolean encontroClase = false;
		/*solo elige uno de los bloques donde w es outsider*/
		while(itt.hasNext()){
			claseApuntada= (Integer)itt.next();
			if(L.keySet().contains(claseApuntada)&& (encontroClase=true))break;	
		}
		/*Me curo en patologias, si no encuentra ningun bloque, esto nunca deberia pasar. porver*/
		if(!encontroClase){
			out.remove(w);//it.remove();
			outStack.remove(w);
			continue;
		}
		bloque S = L.get(claseApuntada);
		int hS = S.hash;
		/*izq\cup der = S, los dos bloques que refinan la particion pasada
		der = {e\in S:(w,e)\in E}
		izq = {e\in S:(w,e)\not \in E}
		*/
		HashSet<Integer> izq = new HashSet<Integer>();
		HashSet<Integer> der = new HashSet<Integer>();
		int repreIzq = -1;
		int repreDer = -1;
		//System.out.println(CuadDescMod.ady);
		//System.out.println("Antes Refi "+S);
		for(int e: S.elementos){
			if(CuadDescMod.ady.get(w).contains(e)){
				der.add(e);repreDer=e;
			}else{ izq.add(e);repreIzq = e;}
		}
		/*refinamiento de S en iz,der*/
		if(izq.size()>0 && der.size()>0){
			L.remove(hS);
			HashSet<Integer> izO = new HashSet<Integer>();
			HashSet<Integer> derO = new HashSet<Integer>();
			/*los nuevos bloques deben quedar apuntando a sus outsiders, se elimina el bloque hS de los outsiders*/
			for(int outS:S.outsiders){
				if(outS==w)continue;
				izO.add(outS);
				derO.add(outS);
				if(!out.keySet().contains(outS))
					out.put(outS,new HashSet<Integer>());
				out.get(outS).add(ha);
				out.get(outS).add(ha+1);
				out.get(outS).remove(hS);
				outStack.add(outS);
			}
			/*los outsiders se cruzan entre los nuevos dos bloques (esto es lineal porque es una 2 estructura(solo 1 tipo de ady).)*/
			for(int outDer:der){
				if(outDer==w)continue;
				izO.add(outDer);
				if(!out.keySet().contains(outDer))
					out.put(outDer,new HashSet<Integer>());
				if(!outStack.contains(outDer))outStack.add(outDer);
					out.get(outDer).add(ha);
			}
			/*se crea el bloque izq*/
			L.put(ha,new bloque(ha,repreIzq,izq,izO));
			repreBlo.put(repreIzq,ha);
			ha++;
			for(int outDer:izq){
				if(outDer==w)continue;
				derO.add(outDer);
				if(!out.keySet().contains(outDer))
					out.put(outDer,new HashSet<Integer>());
				if(!outStack.contains(outDer))outStack.add(outDer);
					out.get(outDer).add(ha);
			}
			/*se crea el bloque der*/
			L.put(ha,new bloque(ha,repreDer,der,derO));
			repreBlo.put(repreDer,ha);
			ha++;
			out.get(w).remove(hS);
		}else{ // si todos son indistinguibles solo se elimina el outsider
			out.get(w).remove(hS);
			L.get(hS).outsiders.remove(w);
		}

		if(out.get(w).size()==0){
			out.remove(w);//it.remove();
			outStack.remove(w);
		}
		//System.out.println("Refi(w="+w+" "+hS+"): L="+L+" outsiders="+out+" outS="+outStack);
		}
		/*se crea el bloque que falta, el que solo contiene el pivot*/
		//singleV.add(pivot);
		HashSet<Integer> singleVV = new HashSet<Integer>();
		singleVV.add(pivot);
		L.put(ha,new bloque(ha,pivot,singleVV,null));
		repreBlo.put(pivot,ha);
		L.get(ha).pivot=true;//se da un flag para saber que bloque es el del pivot
	}
	public String toString(){
		return "Repre="+repreBlo+"L="+L+" out="+out;
	}
}
/*
clase bloque, compone clase part(particion)
*/
class bloque{
	public boolean pivot=false;//es el bloque del pivot?
	public int hash;//identificador del bloque en L(particion)
	public int repre;//repre \in elementos.
	public HashSet<Integer> outsiders;
	public HashSet<Integer> elementos;
	public int iz,der;
	/*recibe su hash, los elementos, los outsiders*/
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
/*
clase nodo, compone a arbol.
contiene el tipo de nodo(primo degenerado)
0 vacio
1 primitivo
2 completo
3 hoja
*/
class nodo{
	public HashSet<Integer> hijos;
	public HashSet<Integer> et;
	public int clase;
	public nodo(int clase,HashSet<Integer> et){
		this.clase = clase;
		hijos = new HashSet<Integer>();
		this.et = et;
	}
	public String toString(){
		String ja = "";//+hijos;
		int cantHijos = hijos.size();
		for(int n:hijos)
			ja+= n+"->"+CuadDescMod.ptf.get(n)+" ";
		String tipoImpresion = clase==1?"Primitivo":clase==0?"Vacio":(clase==3?"Hoja":"Completo");
		return tipoImpresion+" "+(clase==3?et:cantHijos+" ("+ja+")");
	}
}
/*
clase arbol, tiene un nodo raiz y de recorre recursivamente
*/
class arbol{
	public nodo raiz;
	public arbol(int a,HashSet<Integer> b){
		raiz = new nodo(a,b);
	}
	public String toString(){
		return ""+raiz;
	}
}
