// File: MainInterface.java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainInterface extends JFrame {
    private static final ArrayList<JobOffer> jobOffers = Scraper.jobOffers;
    private static JobOffersTable jobOffersTable;
    private static SearchPanel searchBar;
    private static final Logger logger = LogManager.getLogger(Scraper.class);



    public MainInterface() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Job Offers");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        jobOffersTable = new JobOffersTable();
        searchBar = new SearchPanel();
        searchBar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 100));

        add(searchBar, BorderLayout.NORTH);
        add(jobOffersTable, BorderLayout.CENTER);

        customizeAppearance();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void customizeAppearance() {
        try {
            getContentPane().setBackground(UIVariables.backgroundColor2);
            setForeground(UIVariables.foregroundColor);

            searchBar.setForeground(UIVariables.foregroundColor);
            jobOffersTable.setForeground(UIVariables.foregroundColor);
        } catch (Exception e) {
            logger.error("Failed to customize appearance");
        }

    }

    public static void searchJobOffers() {

        if (Scraper.nfjScrapingFlag || Scraper.jjitScrapingFlag) {
            try {
                JOptionPane.showMessageDialog(null, "Nie można szukać nowych ofert podczas scrapowania", "Uwaga", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                logger.error("Cant open dialog window");
            }

            logger.warn("Can't scrape new data while previous scraping process is still active");
            return;
        }

        jobOffers.clear();


        try {
            String inputOfferName = searchBar.getOfferName();
            String inputLocation = searchBar.getOfferLocation();
            String inputExpLevel = searchBar.getExpLevel();

            if ("dowolna".equals(inputLocation)) {
                inputLocation = "";
            }
            if ("dowolny".equals(inputExpLevel)) {
                inputExpLevel = "";
            }

            String[] urls = UrlModifier.getModifiedUrls(inputLocation, inputOfferName, inputExpLevel);
            String nofluffjobsUrl = urls[0];
            String justjoinitUrl = urls[1];


            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(() -> Scraper.getNfjData(nofluffjobsUrl));
            executor.execute(() -> Scraper.getJjitData(justjoinitUrl));

            Timer timer = new Timer(1000, e -> jobOffersTable.updateTable());
            timer.start();

            executor.shutdown();

        } catch (Exception e) {
            logger.fatal("Fatal error, cannot start the scraping porcess");
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainInterface::new);
    }
}
