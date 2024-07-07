package com.irg.ftpserver.service;

import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.DefaultCloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.util.threads.CloseableExecutorService;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class SFTPCustomCloseableExecutorService extends AbstractExecutorService implements CloseableExecutorService {
    private final SFTPExecutorService delegate;
    private final List<SshFutureListener<CloseFuture>> listeners = new CopyOnWriteArrayList<>();
    private final DefaultCloseFuture closeFuture;

    public SFTPCustomCloseableExecutorService(SFTPExecutorService delegate) {
        this.delegate = delegate;
        closeFuture = new DefaultCloseFuture(null,false);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        return delegate.getThreadPoolExecutor().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.getThreadPoolExecutor().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.getThreadPoolExecutor().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        return delegate.getThreadPoolExecutor().awaitTermination(timeout, unit);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        delegate.getThreadPoolExecutor().execute(command);
    }

    @Override
    public CloseFuture close(boolean immediately) {
        if(immediately){
            delegate.getThreadPoolExecutor().shutdownNow();
        }else{
            delegate.getThreadPoolExecutor().shutdown();
        }
        listeners.forEach(listener -> listener.operationComplete(closeFuture));
        closeFuture.setClosed();
        return closeFuture;
    }

    @Override
    public void addCloseFutureListener(SshFutureListener<CloseFuture> sshFutureListener) {
        listeners.add(sshFutureListener);
    }

    @Override
    public void removeCloseFutureListener(SshFutureListener<CloseFuture> sshFutureListener) {
        listeners.remove(sshFutureListener);
    }

    @Override
    public boolean isClosed() {
        return closeFuture.isClosed();
    }

    @Override
    public boolean isClosing() {
        return !closeFuture.isDone(); // Check if the future is not yet done
    }
}
