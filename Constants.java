package com.snowcoin.snowcoin;

/**
 * Created by sookmyung on 2018-02-04.
 */

public class Constants {
    public static enum Status {
        OWN_TRANSACTION,
        NO_PUBLIC_KEY,
        INCORRECT_NONCE,
        FUTURE_BLOCK,
        BAD_HASH,
        BAD_SIGNATURE,
        BAD_INPUTS,
        DUPLICATE,
        SUCCESS,
        UNKNOWN
    };
}
