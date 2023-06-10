package Visual;

import InterfaceLink.BoardLink;

import javax.swing.*;
import java.awt.*;
/**
 Klasa PlayerScorePanel reprezentuje panel wyświetlający wynik gracza.
 */
public class PlayerScorePanel extends JPanel {
    private final BoardLink boardLink;
    /**
     Konstruktor klasy PlayerScorePanel.
     @param boardLink Obiekt typu BoardLink, który umożliwia połączenie z logiką gry.
     */
    public PlayerScorePanel (BoardLink boardLink){
        this.boardLink = boardLink;
        setBackground(Color.GRAY);
    }
    /**
     Metoda paintComponent wywoływana automatycznie przy potrzebie ponownego rysowania panelu.
     Rysuje tekst z aktualnym wynikiem gracza na panelu.
     @param g Obiekt typu Graphics, który umożliwia rysowanie na panelu.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String scoreText = "Score: " + boardLink.getPLayerScore();
        FontMetrics fontMetrics = g.getFontMetrics();
        int stringWidth = fontMetrics.stringWidth(scoreText);
        int stringHeight = fontMetrics.getHeight();
        int x = (getWidth() - stringWidth) / 2;
        int y = (getHeight() - stringHeight) / 2 + fontMetrics.getAscent();
        g.drawString(scoreText, x, y);
    }
}
