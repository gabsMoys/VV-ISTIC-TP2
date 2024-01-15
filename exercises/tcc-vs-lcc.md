# TCC *vs* LCC

Explain under which circumstances *Tight Class Cohesion* (TCC) and *Loose Class Cohesion* (LCC) metrics produce the same value for a given Java class. Build an example of such as class and include the code below or find one example in an open-source project from Github and include the link to the class below. Could LCC be lower than TCC for any given class? Explain.

A refresher on TCC and LCC is available in the [course notes](https://oscarlvp.github.io/vandv-classes/#cohesion-graph).

## Answer


Deux scénarios où LCC = TCC : 

1 : Une classe avec une cohésion maximale, chaque méthode partage au moins une variable d'instance avec une autre méthode.
Dans cet exemple, LCC = TCC = 1.

```
public class HighCohesion {
	private int x,y,z;
	
	public void m1() {
		System.out.println("x + y =" + x+y);
	}
	
	public int m2() {
		return x + z;
	}
	
	public void m3(int i) {
		z = i*y;
	}
}
```

2 : Une classe avec une cohésion minimale, l'opposé de l'exemple précédent.
Dans cet exemple, LCC = TCC = 0.

```
public class LowCohesion {
	private int x,y,z;
	
	public void m1() {
		System.out.println("x=" + x);
	}
	
	public int m2() {
		return y;
	}
	
	public void m3(int i) {
		if(z != 0)
			z = i;
	}
}
```

Could LCC be lower than TCC for any given class?

TCC comptabilise les connexions directes, LCC comptabilise les connexions directes et/ou indirectes.
Ainsi LCC comptabilisera au moins autant de connexions que TCC.
Il est donc impossible d'obtenir LCC < TCC.




















