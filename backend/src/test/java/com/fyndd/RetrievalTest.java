package com.fyndd;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RetrievalTest {
 @Test void retrievesRelevantCatalogData(){Products products=mock(Products.class);var headphones=new Product();headphones.name="Wireless headphones";headphones.category="Electronics";headphones.description="Audio for music";var tote=new Product();tote.name="Canvas tote";tote.category="Accessories";tote.description="Carry your essentials";when(products.findAll()).thenReturn(List.of(headphones,tote));var search=new CatalogCache(products);assertEquals(headphones,search.retrieve("headphones").getFirst());assertEquals(1.0,CatalogCache.similarity(CatalogCache.vector("wireless headphones"),CatalogCache.vector("wireless headphones")),1e-9);verify(products).findAll();}
}
