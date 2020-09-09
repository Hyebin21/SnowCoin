package com.snowcoin.snowcoin;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Blockchain {

    public static final String              NO_ONE              = "no one";

    //임의로
    public static int[] tid = {1};
    /*
    public static final Signature           NO_ONE_SIGNATURE;
    public static final byte[]              NO_ONE_PUB_KEY;

    static {
        try {
            final KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA", "SUN");
            final SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            gen.initialize(512, random);

            final KeyPair pair = gen.generateKeyPair();
            final PrivateKey privateKey = pair.getPrivate();
            NO_ONE_SIGNATURE = Signature.getInstance("SHA1withDSA", "SUN");
            NO_ONE_SIGNATURE.initSign(privateKey);

            final PublicKey publicKey = pair.getPublic();
            NO_ONE_PUB_KEY = publicKey.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    */

    public static final String              GENESIS_NAME        = "genesis";
    protected static final boolean          DEBUG               = Boolean.getBoolean("debug");
    private static final Transaction        GENESIS_TRANS;
    private static final Block              GENESIS_BLOCK;
    private static final String INITIAL_HASH = "7982970534e089b839957b7e174725ce1878731ed6d700766e59cb16f1c25111";
    private static final String nextHash = "7982970534e089b839957b7e174725ce1878731ed6d700766e59cb16f1c21111";

    //초기 블록 생성
    static {
        GENESIS_TRANS = new Transaction(1, NO_ONE, GENESIS_NAME, 30, "02/12", 1);
        GENESIS_BLOCK = new Block("no one", 0, INITIAL_HASH, nextHash, tid, 0);
    }

    private final List<Block>               blockchain          = new CopyOnWriteArrayList<Block>();
    private final List<Transaction>         transactions        = new CopyOnWriteArrayList<Transaction>();

    private final String                    owner;

    private volatile String                 latestHash          = "7982970534e089b839957b7e174725ce1878731ed6d700766e59cb16f1c25e11";

    public Blockchain(String owner) {
        this.owner = owner;
        // transfer initial coins to genesis entity
        //   this.addBlock(GENESIS_NAME, GENESIS_BLOCK);
    }
    /*
        public int getLength() {
            return blockchain.size();
        }

        public Block getBlock(int blockNumber) {
            if (blockNumber>=blockchain.size())
                return null;
            return blockchain.get(blockNumber);
        }

        public Block getNextBlock(String name, Transaction[] transactions) {
            int length = 0;
            for (Transaction transaction : transactions)
                length += transaction.getBufferLength();
            final byte[] bytes = new byte[length];
            final ByteBuffer bb = ByteBuffer.wrap(bytes);
            for (Transaction transaction : transactions) {
                final ByteBuffer buffer = ByteBuffer.allocate(transaction.getBufferLength());
                transaction.toBuffer(buffer);
                buffer.flip();
                bb.put(buffer.array());
            }
            final byte[] nextHash = getNextHash(latestHash, bytes);
            return (new Block(name, latestHash, nextHash, transactions, this.blockchain.size()));
        }

        public Constants.Status checkHash(Block block) {
            if (block.blockLength > this.blockchain.size()) {
                // This block is in the future
                if (DEBUG)
                    System.out.println(owner+" found a future block. lengths="+this.blockchain.size()+"\n"+"block={\n"+block.toString()+"\n}");
                return Constants.Status.FUTURE_BLOCK;
            }

            int length = 0;
            for (Transaction transaction : block.transactions)
                length += transaction.getBufferLength();
            final byte[] bytes = new byte[length];
            final ByteBuffer bb = ByteBuffer.wrap(bytes);

            for (Transaction transaction : block.transactions) {
                final ByteBuffer buffer = ByteBuffer.allocate(transaction.getBufferLength());
                transaction.toBuffer(buffer);
                buffer.flip();
                bb.put(buffer.array());
            }

            // Calculate what I think the next has should be
            final byte[] nextHash = getNextHash(latestHash, bytes);

            // Store the previous and next hash from the block
            final byte[] incomingPrev = block.prev;
            final byte[] incomingHash = block.hash;

            if (!(Arrays.equals(incomingHash, nextHash))) {
                // This block has a different 'next' hash then I expect, someone is out of sync
                if (DEBUG) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(owner).append(" Invalid hash in block\n");
     //               builder.append("confirmed="+block.confirmed).append("\n");
                    builder.append("length=").append(this.blockchain.size()).append("\n");
                    builder.append("latest=["+HashUtils.bytesToHex(latestHash)+"]\n");
                    builder.append("next=["+HashUtils.bytesToHex(nextHash)+"]\n");
                    builder.append("incomingLength=").append(block.blockLength).append("\n");
                    builder.append("incomingPrev=["+HashUtils.bytesToHex(incomingPrev)+"]\n");
                    builder.append("incomingNext=["+HashUtils.bytesToHex(incomingHash)+"]\n");
                    System.err.println(builder.toString());
                }
                return Constants.Status.BAD_HASH;
            }

            return Constants.Status.SUCCESS;
        }

        public Constants.Status addBlock(String dataFrom, Block block) {
            // Already processed this block? Happens if a miner is slow and isn't first to confirm the block
            if (blockchain.contains(block))
                return Constants.Status.DUPLICATE;

            // Check to see if the block's hash is what I expect
            final Constants.Status status = checkHash(block);
            if (status != Constants.Status.SUCCESS)
                return status;

            // Get the aggregate transaction for processing
                // Remove the inputs from the unused pool

            // Add outputs to unused pool

            // Update the hash and add the new transaction to the list
            final byte[] prevHash = latestHash;
            final byte[] nextHash = block.hash;
            blockchain.add(block);
            for (Transaction transaction : block.transactions)
                transactions.add(transaction);
            latestHash = nextHash;

            if (DEBUG) {
                final String prev = HashUtils.bytesToHex(prevHash);
                final String next = HashUtils.bytesToHex(nextHash);
                final StringBuilder builder = new StringBuilder();
                builder.append(owner).append(" updated hash").append(" msg_from='"+dataFrom+"'").append(" block_miner='"+block.miner+"'\n");
                builder.append("blockchain length=").append(blockchain.size()).append("\n");
                builder.append("transactions=[\n");

                for (Transaction t : block.transactions) {
                    builder.append(t.toString()).append("\n");
                }

                builder.append("]\n");
                builder.append("prev=[").append(prev).append("]\n");
                builder.append("next=[").append(next).append("]\n");
                System.err.println(builder.toString());
            }

            return Constants.Status.SUCCESS;
        }

        public long getBalance(String name) {
            long result = 0;
            for (Transaction t : transactions) {
                // Remove the inputs
                // Add the outputs
            }
            return result;
        }
    */
    public static final byte[] getNextHash(byte[] hash, byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(HashUtils.bytesToHex(hash)).append(HashUtils.bytesToHex(bytes));
        final String string = builder.toString();
        final byte[] output = HashUtils.calculateSha256(string);
        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Blockchain))
            return false;

        final Blockchain other = (Blockchain) o;

        for (Block b : blockchain) {
            if (!(other.blockchain.contains(b)))
                return false;
        }
        for (Transaction t : transactions) {
            if (!(other.transactions.contains(t)))
                return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("hash=[").append("7982970534e089b839957b7e174725ce1878731ed6d700766e59cb16f1c25e21").append("]\n");
        return builder.toString();
    }
}