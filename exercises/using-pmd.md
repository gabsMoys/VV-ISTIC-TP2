# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset. Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false positive). Explain why you would not solve this issue.

You can use the default [rule base](https://github.com/pmd/pmd/blob/master/pmd-java/src/main/resources/rulesets/java/quickstart.xml) available on the source repository of PMD.

## Answer


projet choisi : https://github.com/apache/commons-collections

commande lancée : pmd check -f text -R rulesets/java/quickstart.xml -d ~/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4

vrai positive retenu : /home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/CollectionUtils.java:389:	LooseCoupling:	Avoid using implementation types like 'ArrayList'; use the interface instead

le code en question : 

```
public static <O> Collection<O> subtract(final Iterable<? extends O> a,
                                             final Iterable<? extends O> b,
                                             final Predicate<O> p) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        Objects.requireNonNull(p, "p");
        final ArrayList<O> list = new ArrayList<>();
        final HashBag<O> bag = new HashBag<>();
        for (final O element : b) {
            if (p.evaluate(element)) {
                bag.add(element);
            }
        }
        for (final O element : a) {
            if (!bag.remove(element, 1)) {
                list.add(element);
            }
        }
        return list;
    }
```    
ligne considérée pour l'exercice : final ArrayList<O> list = new ArrayList<>();  

Cette ligne ne respecte pas le principe Dependency Inversion (SOLID) pour deux raisons :   
- notre objet list est typé par une implémentation  
- la dépendance est injectée dans le code.  
Cela rend le code moins maintenable, un changement de choix d'implémentation implique d'aller modifier le code partout ou il y a une dépendance à une implementation.  
Une solution pourrait etre de proposer un objet en charge d'injecter les dependances et donc de choisir les implémentations, dans l'idée de pouvoir changer d'implémentation sans  
avoir besoin d'aller modifier la fonction subtract.  

par exemple : final List<O> list = DependencyInjectors.getListImpl();  
On remarque que la seule opération utilisée sur "list" est list.add, ArrayList est un choix acceptable quand on souhaite juste ajouter des élements à la fin de la collection.  
Mais ce n'est valable que pour un ajout à la fin, la vrai bonne implémentation pour des opérations d'ajout et de suppresion est la LinkedList.  
On pourrait donc raffiner notre solution par : final List<O> list = DependencyInjectors.getListImpl(OPERATION_TYPE.BEST_IMPL_FOR_MODIFICATION);  
Où OPERATION_TYPE est une enumeration listant les types d'opérations sur des listes (on veut la meilleur impl de List pour l'operation add).    
Avec cette solution, l'injection de dépendance en centralisé dans une classe DependencyInjectors.  
On remarque que subtract renvoit une Collection, soit une interface de plus haut niveau, on pourrait donc étendre notre solution avec un injecteur de dépendance pour des collections.  





faux positif retenu : home/gaby/VV/input_for_exo_2/commons-collections-master/src/main/java/org/apache/commons/collections4/keyvalue/DefaultKeyValue.java:83:	CompareObjectsWithEquals:	Use equals() to compare object references.

le code en question :  
```
/**
     * Sets the key.
     *
     * @param key  the new key
     * @return the old key
     * @throws IllegalArgumentException if key is this object
     */
    @Override
    public K setKey(final K key) {
        if (key == this) {
            throw new IllegalArgumentException("DefaultKeyValue may not contain itself as a key.");
        }

        return super.setKey(key);
    }
 ```   
    
La comparaison  "if (key == this)" est pertinente, on souhaite bien comparer des références, ajouter une méthode equals ne ferait qu'ajouter une indirection pour au  
final comparer des références, de plus ce serait moins compréhensible, ici on comprend bien que l'on souhaite comparer des références.  


