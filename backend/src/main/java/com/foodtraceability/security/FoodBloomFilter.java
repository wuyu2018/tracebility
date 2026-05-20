package com.foodtraceability.security;

import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Objects;

@Component
public class FoodBloomFilter {
    
    private final BitSet bitSet;
    private final int hashCount;
    private final int size;
    
    public FoodBloomFilter(int expectedElements, double falsePositiveRate) {
        this.size = (int) (-expectedElements * Math.log(falsePositiveRate) 
                          / (Math.log(2) * Math.log(2)));
        this.hashCount = (int) (size / expectedElements * Math.log(2));
        this.bitSet = new BitSet(size);
    }
    
    public FoodBloomFilter() {
        this(100000, 0.01);
    }
    
    public void add(String foodId) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(foodId, i);
            bitSet.set(hash % size);
        }
    }
    
    public boolean mightContain(String foodId) {
        for (int i = 0; i < hashCount; i++) {
            int hash = hash(foodId, i);
            if (!bitSet.get(hash % size)) {
                return false;
            }
        }
        return true;
    }
    
    private int hash(String foodId, int seed) {
        return Objects.hash(seed, foodId.hashCode());
    }
    
    public byte[] toBytes() {
        return bitSet.toByteArray();
    }
    
    public static FoodBloomFilter fromBytes(byte[] bytes, int expectedElements, double falsePositiveRate) {
        FoodBloomFilter filter = new FoodBloomFilter(expectedElements, falsePositiveRate);
        filter.bitSet.or(BitSet.valueOf(bytes));
        return filter;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getHashCount() {
        return hashCount;
    }
}
