package net.kenevans.imagemodel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class PrintPreviewDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    private PrintPreviewPanel canvas;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    /**
     * Constructs a print preview dialog.
     * 
     * @param p a Printable
     * @param pf the page format
     * @param pages the number of pages in p
     */
    public PrintPreviewDialog(Printable p, PageFormat pf, int pages) {
        Book book = new Book();
        book.append(p, pf, pages);
        uiInit(book);
    }

    /**
     * Constructs a print preview dialog.
     * 
     * @param b a Book
     */
    public PrintPreviewDialog(Book book) {
        uiInit(book);
    }

    /**
     * Lays out the UI of the dialog.
     * 
     * @param book The book to be previewed
     */
    public void uiInit(Book book) {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setTitle("Print Preview");

        Container contentPane = getContentPane();

        // Keep it from touching the top
        Box box = Box.createHorizontalBox();
        box.add(Box.createVerticalStrut(5));
        contentPane.add(box, BorderLayout.NORTH);

        canvas = new PrintPreviewPanel(book);
        contentPane.add(canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton nextButton = new JButton("Next");
        buttonPanel.add(nextButton);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                canvas.flipPage(1);
            }
        });

        JButton previousButton = new JButton("Previous");
        buttonPanel.add(previousButton);
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                canvas.flipPage(-1);
            }
        });

        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });

        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }

}
