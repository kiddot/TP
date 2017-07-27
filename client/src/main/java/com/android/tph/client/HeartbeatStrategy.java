package com.android.tph.client;

import com.android.tph.api.Client;
import com.android.tph.api.Constants;
import com.android.tph.api.Logger;
import com.android.tph.api.protocol.Packet;

import static com.android.tph.api.Constants.MAX_HB_TIMEOUT_COUNT;

/**
 * 由于不同的网络环境nat老化的时间不同，采用固定的心跳策略不能很好适应不同的网络环境变化，一般wifi条件
 * 下采用5分钟左右的心跳策略，4g条件下采用十几分钟的心跳策略，仅仅为了适应这两种条件，那么就必须设置最低
 * 的心跳时间间隔，但对于wifi用户来说消耗了不必要的通信资源，故采用自适应的心跳间隔策略，适应不同网络环境
 *
 * Created by kiddo on 17-7-27.
 */

public class HeartbeatStrategy {
    private final Logger logger = ClientConfig.I.getLogger();
    private int mMinHeartbeat;//心跳可选区间
    private int mMaxHeartbeat;
    private int mCurrentHeartbeat;//当前心跳初始值为successHeart
    private int mSuccessHeartbeat;//当前成功心跳，初始为MinHeart
    private int mHeartbeatStep;//心跳增加步长
    private int mSuccessStep;//稳定期后的探测步长
    private int mHeartbeatCount;//心跳失败次数
    private int mMaxTryCount;
    private boolean mIsNeedFix;
    private boolean mIsNeedRunningStrategy;
    private TCPConnection mConnection;

    private static HeartbeatStrategy mHeartbeatStrategy = null;
    private HeartbeatStrategy(){
        initData();
    }

    public static HeartbeatStrategy getInstance(){
        synchronized (HeartbeatStrategy.class){
               if (mHeartbeatStrategy == null){
                    mHeartbeatStrategy = new HeartbeatStrategy();
               }
        }
        return mHeartbeatStrategy;
    }

    private void initData(){
        mMinHeartbeat = Constants.MIN_HEARTBEAT;
        mMaxHeartbeat = Constants.MAX_HEARTBEAT;
        mHeartbeatStep = Constants.HEARTBEAT_STEP;
        mSuccessHeartbeat = mMinHeartbeat;
        mCurrentHeartbeat = mSuccessHeartbeat;
        mMaxTryCount = MAX_HB_TIMEOUT_COUNT;
        mIsNeedFix = true;
        mIsNeedRunningStrategy = false;
    }

    /**
     * 最大值探测步骤（自适应心跳计算流程）
     */
    public void calculateFixHeartbeat(TCPConnection connection){
        if (!mIsNeedFix) return;
        if (isHeartbeatSuccess(connection)){
            //成功
            logger.w("calculateFixHeartbeat:心跳成功");
            mSuccessHeartbeat = mCurrentHeartbeat ;
            mCurrentHeartbeat += mHeartbeatStep;
        } else {
            logger.w("calculateFixHeartbeat：心跳失败");
            mHeartbeatCount ++;
            if (mHeartbeatCount >= mMaxTryCount){
                //结束
                mIsNeedFix = false;
                mIsNeedRunningStrategy = true;
            }
        }
    }

    /**
     * 运行时的动态调整策略(已经按测算心跳稳定值后)
     */
    public void calculateRunningStrategy(TCPConnection connection){
        if (!mIsNeedRunningStrategy) return;
        if (isHeartbeatSuccess(connection)){
            //成功
            logger.w("calculateRunningStrategy:心跳成功");
            mHeartbeatCount = 0;
        } else {
            logger.w("calculateRunningStrategy：心跳失败");
            mHeartbeatCount ++;
            if (mHeartbeatCount >= mMaxTryCount){
                mSuccessHeartbeat = mMinHeartbeat;
                mCurrentHeartbeat = mSuccessHeartbeat;
                mIsNeedFix = true;
                mIsNeedRunningStrategy = false;
            }
        }
    }

    private boolean isHeartbeatSuccess(TCPConnection connection){
        if (connection.isReadTimeout()) {
            mHeartbeatCount++;
            logger.w("heartbeat timeout times=%s", mHeartbeatCount);
        } else {
            mHeartbeatCount = 0;
        }

        if (mHeartbeatCount >= mMaxTryCount + 1) {
            logger.w("heartbeat timeout times=%d over limit=%d, client restart", mHeartbeatCount, mMaxTryCount);
            mHeartbeatCount = 0;
            mCurrentHeartbeat = mSuccessHeartbeat;
            connection.reconnect();
            return false;
        }

        if (connection.isWriteTimeout()) {
            logger.d("<<< send heartbeat ping...");
            connection.send(Packet.HB_PACKET);
        }

        return true;
    }

    public int getmMinHeartbeat() {
        return mMinHeartbeat;
    }

    public void setmMinHeartbeat(int mMinHeartbeat) {
        this.mMinHeartbeat = mMinHeartbeat;
    }

    public int getmMaxHeartbeat() {
        return mMaxHeartbeat;
    }

    public void setmMaxHeartbeat(int mMaxHeartbeat) {
        this.mMaxHeartbeat = mMaxHeartbeat;
    }

    public int getmCurrentHeartbeat() {
        return mCurrentHeartbeat;
    }

    public void setmCurrentHeartbeat(int mCurrentHeartbeat) {
        this.mCurrentHeartbeat = mCurrentHeartbeat;
    }

    public int getmSuccessHeartbeat() {
        return mSuccessHeartbeat;
    }

    public void setmSuccessHeartbeat(int mSuccessHeartbeat) {
        this.mSuccessHeartbeat = mSuccessHeartbeat;
    }

    public int getmHeartbeatStep() {
        return mHeartbeatStep;
    }

    public void setmHeartbeatStep(int mHeartbeatStep) {
        this.mHeartbeatStep = mHeartbeatStep;
    }

    public int getmSuccessStep() {
        return mSuccessStep;
    }

    public void setmSuccessStep(int mSuccessStep) {
        this.mSuccessStep = mSuccessStep;
    }

    public static HeartbeatStrategy getmHeartbeatStrategy() {
        return mHeartbeatStrategy;
    }

    public static void setmHeartbeatStrategy(HeartbeatStrategy mHeartbeatStrategy) {
        HeartbeatStrategy.mHeartbeatStrategy = mHeartbeatStrategy;
    }

    public boolean ismIsNeedRunningStrategy() {
        return mIsNeedRunningStrategy;
    }

    public void setmIsNeedRunningStrategy(boolean mIsNeedRunningStrategy) {
        this.mIsNeedRunningStrategy = mIsNeedRunningStrategy;
    }

    public boolean ismIsNeedFix() {
        return mIsNeedFix;
    }

    public void setmIsNeedFix(boolean mIsNeedFix) {
        this.mIsNeedFix = mIsNeedFix;
    }
}
