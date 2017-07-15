package com.android.server.boot.job;

/**
 * Created by kiddo on 17-7-15.
 */

public class BootChain {
    private final BootJob boot = new BootJob() {
        {
        }

        @Override
        protected void start() {
            startNext();
        }

        @Override
        protected void stop() {
            stopNext();
            System.out.print("bootstrap chain stopped.");
            System.out.print("===================================================================");
            System.out.print("====================MPUSH SERVER STOPPED SUCCESS===================");
            System.out.print("===================================================================");
        }
    };

    private BootJob last = boot;

    public void start() {
        boot.start();
    }

    public void stop() {
        boot.stop();
    }

    public static BootChain chain() {
        return new BootChain();
    }

    public BootChain boot() {
        return this;
    }

    public void end() {
        setNext(new BootJob() {
            @Override
            protected void start() {
                System.out.print("bootstrap chain started.");
                System.out.print("===================================================================");
                System.out.print("====================MPUSH SERVER START SUCCESS=====================");
                System.out.print("===================================================================");
            }

            @Override
            protected void stop() {
                System.out.print("bootstrap chain stopping...");
            }

            @Override
            protected String getName() {
                return "LastBoot";
            }
        });
    }

    public BootChain setNext(BootJob bootJob) {
        this.last = last.setNext(bootJob);
        return this;
    }

}
