package com.company;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Formatter;

import com.panforge.robotstxt.RobotsTxt;
import crawlercommons.filters.basic.BasicURLNormalizer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Worker extends Thread {
    String url="";
    boolean getLinks;
    private static final String user_agent =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    public Worker(String website,boolean get_links)
    {
        getLinks=get_links;
        url=website;

    }
    public void run() {
        Connection connection = Jsoup.connect(url).userAgent(user_agent);
        Document htmlDocument = null;
        Dbhandler db=new Dbhandler();



            //System.out.println("**Failure** Retrieved something other than HTML");


            try {
                htmlDocument = connection.get();

                if (connection.response().statusCode() == 200) // 200 is the HTTP OK status code
                // indicating that everything is great.
                {
                  processDoc(htmlDocument);
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("Error in out HTTP request " + e);
                try {
                    this.sleep(2000);
                    try {
                        htmlDocument = connection.get();
                        if (connection.response().statusCode() == 200) // 200 is the HTTP OK status code
                        // indicating that everything is great.
                        {
                            System.out.println("\n**Visiting2** Received web page at " + url);
                            processDoc(htmlDocument);

                        }
                    } catch (IOException e1) {
                        db.deleteLink(url);
                        System.out.println("Error in out HTTP request " + e);

                    }
                } catch (InterruptedException e1) {
                    System.out.println("failed to sleep");
                }




            }




    }
  private   void processDoc(Document htmlDocument)
    {
        Dbhandler db=new Dbhandler();
        System.out.println("\n**Visiting** Received web page at " + url);
        String fileName;
        if(getLinks) {
            Elements linksOnPage = htmlDocument.select("a");


            for (Element link : linksOnPage) {


                if (Helper.checkRobot(url))

                {
                    if ((Helper.htmlCheck(link.absUrl("href")))) {

                     //   System.out.println(link.absUrl("href"));
                        try {

                            db.addSite(Helper.urlNmormalizer(link.absUrl("href")));
                        } catch (ClassNotFoundException e) {

                            e.printStackTrace();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
            try {
                fileName = Helper.htmlToFile(url, htmlDocument.html().toString(), db.getID(url));
                System.out.println(fileName);
                db.addContent(url, fileName);
                System.out.println("successfully added");
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


    }




}
