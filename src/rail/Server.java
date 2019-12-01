package rail;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Это консольный чат между "Клиентом" и "Сервером".
 * В данном классе реализована Серверная часть и он долежн запускаться первым.
 *
 * Класс создает соединение через Socket и ServerSocket, присоединяет к полученному socket-у сканнер входящего потока
 * и PrintWriter. А также инициализирует сканнер потока ввода из консоли.
 * Затем создаются два потока.
 * В readingThread - в цикле осущестляется постоянное чтение ходящего потока и трансляция его в консоль;
 * В sendingThread - в цикле осущестляется постоянное чтение потока из консоли и трансляция его в исходящий поток через
 * PrintWriter.
 */

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(8383);
            socket = serverSocket.accept();
            System.out.println("Клиент подключился");

            Scanner scannerIn = new Scanner(socket.getInputStream());
            Scanner scannerConsole = new Scanner(System.in);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            /**
             * Реализация через два потока нужна, так как если реализовать все в главном потоке в одном цикл, то
             * мы будем делать все последоательно. Например, сначала ждать, пока нам прилетит что-нибудть во входящий
             * поток (scannerIn), и только после того как что-то получим, то мы пойдем к следующему оператору и прочитаем
             * что же мы за это время попытались отправить в консоли. Или наоборот, сначала будем ждать пока что-то будет написано
             * в консоле, а уже затем будет читать что же нам прилетело в входящие пока там в консоли набирали и нажимали
             * Enter - в зависимости от того в каком порядке будут указаны операторы в нашем цикле.
             * То есть производить получение и отправку сообщение "в режиме реального времени" при реализации всего в одном
             * главном потоке не получится.
             * А при реализации с двумя потоками у нас параллельно независимо друг от друга крутятся два цикла - один читает
             * и выводит входящие, а второй читает и отправляет сообщения из консоли.
             */

            //Прием сообщений
            Thread readingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        String str = scannerIn.nextLine();
                        if (str.equals("/end")){
                            System.out.println(str);
                            break;
                        }
                        System.out.println("Сообщение от клиента: " + str);
                    }
                }
            });
            readingThread.start();


            //Отправка сообщений
            Thread sendingThreat = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        String str2 = scannerConsole.nextLine();
                        printWriter.println(str2);
                    }
                }
            });
            sendingThreat.start();

            /**
             * Здесь важно остановить основной поток пока работает поток приема сообщений, потому что он обращается
             * к сокету socket. Если не заставит основной поток подождать окончания работы потока приема сообщений,
             * то сработает finally из блока try-catch и закроет сокет. А без него не будет работать scannerIn и при компиляции
             * выдаст ошибку "java.util.NoSuchElementException: No line found".
             */
            try {
                readingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                serverSocket.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
