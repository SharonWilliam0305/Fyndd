package com.fyndd;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import java.util.*;
@Service class CatalogCache {
 private final Products products;
 CatalogCache(Products products){this.products=products;}
 @Cacheable("catalog") public List<Product> all(){return products.findAll();}
 // A local feature-hashed text embedding teaches vector retrieval without API charges.
 // It measures word overlap, not semantic similarity from a trained embedding model.
 static double[] vector(String text){double[] v=new double[256];for(String w:text.toLowerCase(Locale.ROOT).split("\\W+"))if(w.length()>2)v[Math.floorMod(w.hashCode(),v.length)]++;double norm=Math.sqrt(Arrays.stream(v).map(x->x*x).sum());if(norm>0)for(int i=0;i<v.length;i++)v[i]/=norm;return v;}
 static double similarity(double[] a,double[] b){double s=0;for(int i=0;i<a.length;i++)s+=a[i]*b[i];return s;}
 public List<Product> retrieve(String query){double[] q=vector(query);return all().stream().filter(p->similarity(q,vector(p.name+" "+p.category+" "+p.description))>.05).sorted(Comparator.comparingDouble((Product p)->similarity(q,vector(p.name+" "+p.category+" "+p.description))).reversed()).limit(4).toList();}
}
