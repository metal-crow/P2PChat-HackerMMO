package game;

import java.util.HashMap;

import org.javatuples.Triplet;

public class Player {

    //store ability name, if ability unlocked, cooldown max, and current cooldown
    private HashMap<String, Triplet<Boolean,Integer,Integer>> unlockedAbilites=new HashMap<String, Triplet<Boolean,Integer,Integer>>();
    private int exp=0;
    public static final String[] abilities={"kick","disable","scramble","forceblock","viewall"};
    public boolean scrambled=false;
    public boolean viewall=false;
    
    public Player() {
        //add all abilities
        unlockedAbilites.put(abilities[0], Triplet.with(true,10,0));
        unlockedAbilites.put(abilities[1], Triplet.with(false,15,0));
        unlockedAbilites.put(abilities[2], Triplet.with(false,100,0));
        unlockedAbilites.put(abilities[3], Triplet.with(false,30,0));
        unlockedAbilites.put(abilities[4], Triplet.with(true,0,0));
    }
    
    public boolean hasAbility(String name){
        return unlockedAbilites.get(name).getValue0();
    }
    
    public int cooltimeleft(String name){
        return unlockedAbilites.get(name).getValue2();
    }
    
    public void unlockAbility(String name,int cooldown){
        unlockedAbilites.put(name,Triplet.with(true,cooldown,0));
    }

    public void cooldown(String string) {
        Triplet<Boolean,Integer,Integer> o=unlockedAbilites.get(string);
        o=o.setAt2(o.getValue1());
        unlockedAbilites.put(string, o);
    }
    
    public void cooldowntick(String a){
        Triplet<Boolean,Integer,Integer> o=unlockedAbilites.get(a);
        o=o.setAt2(o.getValue2()-1);
        unlockedAbilites.put(a, o);
    }
    
    public void gainxp(){
        exp++;
        if(exp==60){
            //unlock disable ability
            unlockedAbilites.put("disable", Triplet.with(true,15,0));
        }
        if(exp==80){
            //unlock forceblock ability
            unlockedAbilites.put("forceblock", Triplet.with(true,30,0));
        }
        if(exp==210){
            //unlock scramble ability
            unlockedAbilites.put("scramble", Triplet.with(true,100,0));
        }
    }

}
