
package nemat.application.form.other;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import nemat.menu.mode.ModelCard;


public class card extends javax.swing.JPanel {
private Color colorGradient;

    public Color getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(Color colorGradient) {
        this.colorGradient = colorGradient;
    }
    public card() {
        initComponents();
        setOpaque(false);
        setBackground(new Color(37, 99, 235));
        colorGradient = new Color(56, 189, 248);
    }
public void setData(ModelCard data){
    DecimalFormat df=new DecimalFormat();
 lbtitle.setText(data.getTitle());
 lbv.setText(df.format(data.getValues()));
 ic.setIcon(data.getIcon());
 lbp.setText(df.format(data.getPercentage())+"%");
 
}
    /** Replaces the percentage text with a short note (for cards where a percentage makes no sense). */
    public void setNote(String note) {
        lbp.setFont(new java.awt.Font("Roboto", 0, 14));
        lbp.setText(note);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbv = new javax.swing.JLabel();
        lbtitle = new javax.swing.JLabel();
        ic = new javax.swing.JLabel();
        lbp = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lbv.setFont(new java.awt.Font("Roboto", 1, 28)); // NOI18N
        lbv.setForeground(new java.awt.Color(255, 255, 255));
        lbv.setText("Values");

        lbtitle.setFont(new java.awt.Font("Roboto", 1, 16)); // NOI18N
        lbtitle.setForeground(new java.awt.Color(255, 255, 255));
        lbtitle.setText("Title");

        ic.setFont(new java.awt.Font("Roboto", 1, 28)); // NOI18N
        ic.setForeground(new java.awt.Color(255, 255, 255));

        lbp.setFont(new java.awt.Font("Roboto", 1, 28)); // NOI18N
        lbp.setForeground(new java.awt.Color(255, 255, 255));
        lbp.setText("0%");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbp, 0, 120, Short.MAX_VALUE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbtitle, 0, 170, Short.MAX_VALUE)
                            .addComponent(lbv, 0, 130, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                        .addComponent(ic, 0, 20, 40)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbtitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbv, 0, 130, Short.MAX_VALUE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(ic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbp, 0, 120, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();
        int arc = 24;

        // Soft shadow
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillRoundRect(4, 7, Math.max(0, w - 8), Math.max(0, h - 8), arc, arc);

        // Premium gradient
        GradientPaint gradient = new GradientPaint(
                0, 0, getBackground(),
                w, h, colorGradient
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, Math.max(0, w - 4), Math.max(0, h - 6), arc, arc);

        // Subtle highlight
        g2.setComposite(AlphaComposite.SrcOver.derive(0.10f));
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(1, 1, Math.max(0, w - 6), Math.max(0, h / 3), arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ic;
    private javax.swing.JLabel lbp;
    private javax.swing.JLabel lbtitle;
    private javax.swing.JLabel lbv;
    // End of variables declaration//GEN-END:variables
}
