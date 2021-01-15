package com.diligrp.xtrade.upay.pipeline.client;

import com.openjava.nio.endpoint.AbstractNioClient;
import com.openjava.nio.exception.NioSessionException;
import com.openjava.nio.provider.NioNetworkProvider;
import com.openjava.nio.provider.session.INioSession;
import com.openjava.nio.provider.session.listener.ISessionDataListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 盛京银行NIO客户端
 *
 * @author: brenthuang
 * @date: 2020/12/24
 */
public class SjBankNioClient extends AbstractNioClient {
    // 消息头10字节
    private static final int PROTOCOL_HEAD_SIZE = 10;
    // 加密标识2字节
    private static final int PROTOCOL_FLAG_SIZE = 2;
    // 消息编码
    private static final String CHARSET_GBK = "GBK";
    // 加密标识
    private static final String CHAR_FLAG = "00";
    // 请求超时时间
    private static final long READ_TIMEOUT_IN_MILLIS = 15 * 1000;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private INioSession session;
    private SessionDataContext context;

    public SjBankNioClient(String host, int port, NioNetworkProvider provider) throws IOException {
        super.setHost(host);
        super.setPort(port);
        super.setNetworkProvider(provider);
        session = getSession(context = new SessionDataContext());
    }

    public String sendPipelineRequest(String requestXml) throws IOException {
        try {
            String body = CHAR_FLAG.concat(requestXml);
            int bodySize = body.getBytes(CHARSET_GBK).length;
            String header = StringUtils.rightPad(String.valueOf(bodySize), PROTOCOL_HEAD_SIZE);
            String request = header.concat(body);
            byte[] packet = sendAndReceived(request.getBytes(CHARSET_GBK), READ_TIMEOUT_IN_MILLIS);
            // 忽略加密标识2个字节
            return new String(packet, PROTOCOL_FLAG_SIZE, packet.length - PROTOCOL_FLAG_SIZE, CHARSET_GBK);
        } catch (IOException iex) {
            throw iex;
        } catch (Exception ex) {
            throw new NioSessionException("Invalid data protocol");
        }
    }

    public byte[] sendAndReceived(byte[] packet, long receivedTimeOutInMillis) throws IOException {
        byte[] data;

        try {
            session.send(packet);
            data = context.read(receivedTimeOutInMillis);
        } finally {
            if (session != null) {
                session.destroy();
            }
        }

        return data;
    }

    public static void closeQuietly(SjBankNioClient client) {
        if (client != null && client.session != null) {
            client.session.destroy();
        }
    }

    private class SessionDataContext implements ISessionDataListener {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        private final ByteBuffer headerBuffer = ByteBuffer.allocate(PROTOCOL_HEAD_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        private volatile ByteBuffer bodyBuffer;

        @Override
        public void onDataReceived(INioSession session, byte[] packet) {
            ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();

                try {
                    for (int i = 0; i < packet.length; i++) {
                       if (headerBuffer.hasRemaining()) {
                           headerBuffer.put(packet[i]);
                           continue;
                       } else if (bodyBuffer == null) {
                           try {
                               int bodySize = Integer.parseInt(new String(headerBuffer.array(), CHARSET_GBK).trim());
                               bodyBuffer = ByteBuffer.allocate(bodySize);
                           } catch (Exception ex) {
                               LOG.error("Invalid socket data protocol", ex);
                           }
                       }
                       if (bodyBuffer.hasRemaining()) {
                           bodyBuffer.put(packet[i]);
                       }
                    }
                    // 如果第一个数据包刚刚将HeadBuffer填充完，则此时bodyBuffer可能会为NULL
                    if (bodyBuffer != null && !bodyBuffer.hasRemaining()) {
                        bodyBuffer.flip();
                        // 一次性使用无须重置headerBuffer和bodyBuffer
                        this.condition.signalAll();
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("onDataReceived thread interrupted");
            }
        }

        byte[] read(long receivedTimeOutInMillis) throws NioSessionException {
            ReentrantLock lock = this.lock;

            try {
                lock.lockInterruptibly();
                try {
                    if (bodyBuffer == null) {
                        this.condition.await(receivedTimeOutInMillis, TimeUnit.MILLISECONDS);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("Session read thread interrupted");
            }

            if (bodyBuffer == null) {
                throw new NioSessionException("Session read data timeout");
            }

            return bodyBuffer.array();
        }
    }
}
