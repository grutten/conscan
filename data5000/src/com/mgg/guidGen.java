package com.mgg;

import java.util.UUID;

public class guidGen {
    public static void main(String args[]) {
    	for (int i = 0; i <500; ++i)
    		System.out.println(UUID.randomUUID().toString());
    }

}



