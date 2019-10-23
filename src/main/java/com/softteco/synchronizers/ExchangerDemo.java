package com.softteco.synchronizers;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Exchanger;

@Slf4j
public class ExchangerDemo {

    public static void main(String[] args) {

        Exchanger<String> ex = new Exchanger<String>();
        new Thread(new PutThread(ex)).start();
        new Thread(new GetThread(ex)).start();
    }


    static class GetThread implements Runnable{

        Exchanger<String> exchanger;
        String message;

        GetThread(Exchanger<String> ex){

            this.exchanger=ex;
            message = "Hello World!";
        }
        public void run(){

            try{
                message=exchanger.exchange(message);
                log.info("GetThread has received: " + message);
            }
            catch(InterruptedException ex){
                log.error(ex.getMessage());
            }
        }
    }

    static class PutThread implements Runnable{

        Exchanger<String> exchanger;
        String message;

        PutThread(Exchanger<String> ex){

            this.exchanger=ex;
            message = "Hello Java!";
        }
        public void run(){

            try{
                message=exchanger.exchange(message);
                log.info("PutThread has received: " + message);
            }
            catch(InterruptedException ex){
                log.error(ex.getMessage());
            }
        }
    }

}

