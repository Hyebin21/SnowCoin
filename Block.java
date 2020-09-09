package com.snowcoin.snowcoin;

import android.content.Context;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
///진짜냥
/*
 * 블록 헤더에 필요한 것은 6가지
 * 소프트웨어 버전 / 바로 앞 블록의 해쉬 / 머클트리해쉬(현재 블록의 해쉬) / 블록 생성시간(타임스탬프) / 채굴 난이도 / nonce
 * 여기서 소프트웨어 버전은 우리에게 필요 없음 -> 삭제
 * 바로 앞 블록의 해쉬 = prev
 * 머클트리해쉬(현재 블록의 해쉬) = hash
 * 블록 생성시간 = timestamp (이건 Transaction.java에 있음)
 * 채굴 난이도 = numberOfZeros
 * nonce = nonce
 */

public class Block {
    //LENGTH들은 버퍼의 공간을 미리 계산하기 위해서 필요
    private static final int    MINER_LENGTH             = 4;
    private static final int    BLOCK_TIME_LENGTH    = 8;
    private static final int    NUM_OF_ZEROS_LENGTH     = 4;
    private static final int    NONCE_LENGTH            = 4;
    private static final int    BLOCK_LENGTH            = 4;
    private static final int    LENGTH_LENGTH           = 4;


    public int                  bid =1;
    public String               miner;         //채굴자
    public int                  miner_coin;         //채굴자가 보상으로 받는 코인. 첫번째 블록이라면 0으로 설정하자.
    public String               block_time;      //블록 생성 시각 (long -> string)
    public int                  numberOfZeros;   //0의 갯수 = 난이도 조절
    public int                  nonce;         //nonce
    public int                  blockLength;   //블록의 길이
    //    public Transaction[]        transactions;   //거래 목록
    public String               prev;         //이전 블록의 해시값
    public String               hash;         //현재 블록의 해시값
    public int[]                tid;

    public Block() { }

    public Block(String miner, int miner_coin, String prev, String hash, int[] tid, int blockLength) {
        this.miner = miner;
        this.miner_coin = miner_coin;
        this.prev = prev;
        this.hash = hash;
        //       this.block_time = 0;
        this.tid = tid;
        this.blockLength = blockLength;
        this.nonce = 0;
    }

    public void updateTimestamp() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        this.block_time = sdf.format(date);
    }

    /*
    public int getBufferLength() {
        int transactionsLength = 0;
        for (Transaction t : transactions)
            transactionsLength += LENGTH_LENGTH + t.getBufferLength();
        return  MINER_LENGTH + miner.length() +
                LENGTH_LENGTH + prev.length +
                LENGTH_LENGTH + hash.length +
                BLOCK_TIME_LENGTH +
                LENGTH_LENGTH + transactionsLength +
                BLOCK_LENGTH +
                NUM_OF_ZEROS_LENGTH +
                NONCE_LENGTH ;

    }


    public void fromBuffer(ByteBuffer buffer) {
        final int mLength = buffer.getInt();
        final byte[] mBytes = new byte[mLength];
        buffer.get(mBytes, 0, mLength);
        miner = new String(mBytes);

        { // previous hash
            final int length = buffer.getInt();
            prev = new byte[length];
            buffer.get(prev);
        }

        { // next hash
            final int length = buffer.getInt();
            hash = new byte[length];
            buffer.get(hash);
        }
        block_time = buffer.getLong();

        int tLength = buffer.getInt();
        transactions =  new Transaction[tLength];
        for (int i=0; i < tLength; i++) {
            int length = buffer.getInt();
            final byte[] bytes = new byte[length];
            buffer.get(bytes);
            final ByteBuffer bb = ByteBuffer.wrap(bytes);
            final Transaction t = new Transaction();
            t.fromBuffer(bb);
            transactions[i] = t;
        }

        blockLength = buffer.getInt();
        numberOfZeros = buffer.getInt();
        nonce = buffer.getInt();

    }

    public void toBuffer(ByteBuffer buffer) {
        final byte[] mBytes = miner.getBytes();
        buffer.putInt(mBytes.length);
        buffer.put(mBytes);

        buffer.putInt(prev.length);
        buffer.put(prev);

        buffer.putInt(hash.length);
        buffer.put(hash);

        buffer.putLong(block_time);

        buffer.putInt(transactions.length);
        for (Transaction t : transactions) {
            buffer.putInt(t.getBufferLength());
            t.toBuffer(buffer);
        }

        buffer.putInt(blockLength);
        buffer.putInt(numberOfZeros);
        buffer.putInt(nonce);
    }
    */

    /*
     * 이 밑의 부분은 내 생각에 나중에 채굴정보를 받고 검증하는데 쓰는거 같음. 일단 주석처리. 주석안에 수정내용은 적용시켜놓았음
     */

    /*
    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += miner.length();
        hashCode += timestamp;
        hashCode += nonce;
        hashCode += blockLength;
        hashCode += numberOfZeros;
        hashCode += transactions.length;
        for (Transaction t : transactions)
            hashCode += t.hashCode();
        for (byte b : prev)
            hashCode += b;
        for (byte b : hash)
            hashCode += b;
        return 31 * hashCode;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Block))
            return false;
        Block c = (Block) o;
        if (!(c.from.equals(from)))
            return false;
       if (timestamp != c.timestamp)
          return false;
        if (nonce != c.nonce)
            return false;
        if (blockLength != c.blockLength)
            return false;
        if (numberOfZeros != c.numberOfZeros)
            return false;
        if (c.transactions.length != this.transactions.length)
            return false;
        { // compare transactions
            for (int i=0; i<c.transactions.length; i++) {
                if (!(c.transactions[i].equals(this.transactions[i])))
                    return false;
            }
        }
        if (!(Arrays.equals(c.prev, prev)))
            return false;
        if (!(Arrays.equals(c.hash, hash)))
            return false;
        return true;
    }
    */


    public String toString(Context context) {

        DBManagerT Tmanager = new DBManagerT(context);
        StringBuilder builder = new StringBuilder();

        builder.append("prev=[").append(prev).append("]\n");
        builder.append("time='").append(block_time).append("\n");
        builder.append("transactions={").append("\n");
        int i=0;
        for(i=0; i<tid.length; i++){
            Transaction tran = Tmanager.selectData(tid[i]);
            String result = tran.toString();
            builder.append(result).append("\n");
        }
        builder.append("}");
        builder.append("numberOfZerosToCompute=").append(numberOfZeros).append("\n");
        builder.append("nonce=").append(nonce).append("\n");
        return builder.toString();
    }

}
