package com.raheel.lab;

/**
 *
 * @author qyuvks
 * Document class for the vector representation of a document
 */
class Doc{
    int id;
    double weights;

    public Doc(int id, double tw) {
        this.id = id;
        this.weights = tw;
    }

    public String toString() {
        String str = id + ": " + weights;
        return str;
    }
}
