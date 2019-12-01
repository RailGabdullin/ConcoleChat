package rail;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * ВНИМАНИЕ: Первым запускать класс Server! Этот класс запускать после него.
 * Это класс "Клиента" нашего консольного чата.
 * Здесь мы создаем клиентский сокет и также, как в сервере навешивает сканнеры и принтрайтер.
 * Реализация приема и отправки сообщений (через два потока) идентична.
 */

public class Client {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket("localhost", 8383);

            Scanner scannerIn = new Scanner(socket.getInputStream());
            Scanner scannerConsole = new Scanner(System.in);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            //Прием сообщений
            Thread readingTread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String str = scannerIn.nextLine();
                        if(str.equals("/serverstop")){
                            System.out.println(str);
                            break;
                        }
                        System.out.println("Сообщение от сервера: " + str);
                    }
                }
            });
            readingTread.start();

            //Отправка сообщений
            Thread sendingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        String str2 = scannerConsole.nextLine();
                        printWriter.println(str2);
                    }
                }
            });
            sendingThread.start();

            try {
                readingTread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
