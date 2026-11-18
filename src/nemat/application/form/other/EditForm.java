package nemat.application.form.other;

import nematullah.database.Database;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class EditForm extends javax.swing.JPanel {

    private final StudentForm StudentForm;

    public EditForm(String roomnamber,String Name,String Lname,String fathername,
            String NIC,String phone,String Fal,String DE,String dexit,StudentForm studentForm) {
        initComponents();
        this.StudentForm=studentForm;
        rnamber.setText(roomnamber);
        fname.setText(Name);
        lname.setText(Lname);
        Fathername.setText(fathername);
        nic.setText(NIC);
        txtphone.setText(phone);
        
        
        
faculry.removeAllItems();

faculry.addItem("Computer Science");
faculry.addItem("Engineering");
faculry.addItem("Economics");
faculry.addItem("Law");
faculry.addItem("Education");
faculry.addItem("Medicine");
faculry.setSelectedItem(null);
faculry.setSelectedItem(Fal);
dateE.setText(DE);
dateExit.setText(dexit);

    javax.swing.JPanel centerPanel = new javax.swing.JPanel(new GridBagLayout());
    centerPanel.setOpaque(true);
    centerPanel.add(panelLogin1);

    jScrollPane1.setViewportView(centerPanel);

    lb.putClientProperty(FlatClientProperties.STYLE, ""
            + "font:$h1.font");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lb = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelLogin1 = new nematullah.application.form.PanelLogin();
        lbUser = new javax.swing.JLabel();
        rnamber = new javax.swing.JTextField();
        lbPass = new javax.swing.JLabel();
        update = new javax.swing.JButton();
        lbPass1 = new javax.swing.JLabel();
        lbPass2 = new javax.swing.JLabel();
        lbPass3 = new javax.swing.JLabel();
        lbUser1 = new javax.swing.JLabel();
        lbUser2 = new javax.swing.JLabel();
        faculry = new javax.swing.JComboBox<>();
        lbUser3 = new javax.swing.JLabel();
        dateE = new javax.swing.JTextField();
        lbUser4 = new javax.swing.JLabel();
        Fathername = new javax.swing.JTextField();
        lname = new javax.swing.JTextField();
        dateExit = new javax.swing.JTextField();
        txtphone = new javax.swing.JFormattedTextField();
        fname = new javax.swing.JTextField();
        nic = new javax.swing.JFormattedTextField();

        lb.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb.setText("Edit Student");

        panelLogin1.setBorder(new javax.swing.border.MatteBorder(null));

        lbUser.setText("Room Namber");

        rnamber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rnamberActionPerformed(evt);
            }
        });

        lbPass.setText("First Name");

        update.setText("Save Changes");
        update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateActionPerformed(evt);
            }
        });

        lbPass1.setText("Last Name");

        lbPass2.setText("Father Name");

        lbPass3.setText("NIC");

        lbUser1.setText("A Relaative Phone Namber");

        lbUser2.setText("Faculty");

        faculry.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Computer Science", "Engineering", "law", "Economics", "Language" }));
        faculry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                faculryActionPerformed(evt);
            }
        });

        lbUser3.setText("Date Of Entry");

        dateE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateEActionPerformed(evt);
            }
        });

        lbUser4.setText("Date Of Exit");

        Fathername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FathernameActionPerformed(evt);
            }
        });

        lname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lnameActionPerformed(evt);
            }
        });

        dateExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateExitActionPerformed(evt);
            }
        });

        try {
            txtphone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("07########")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        fname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fnameActionPerformed(evt);
            }
        });

        try {
            nic.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("#####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout panelLogin1Layout = new javax.swing.GroupLayout(panelLogin1);
        panelLogin1.setLayout(panelLogin1Layout);
        panelLogin1Layout.setHorizontalGroup(
            panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogin1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(rnamber)
                    .addComponent(lbUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbPass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbPass1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbPass2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbPass3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lname)
                    .addComponent(Fathername)
                    .addComponent(fname, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                    .addComponent(nic))
                .addGap(72, 72, 72)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbUser1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                    .addComponent(lbUser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(faculry, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbUser3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateE)
                    .addComponent(lbUser4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(update, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateExit)
                    .addComponent(txtphone))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelLogin1Layout.setVerticalGroup(
            panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogin1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelLogin1Layout.createSequentialGroup()
                        .addComponent(lbUser)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rnamber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelLogin1Layout.createSequentialGroup()
                        .addComponent(lbUser1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtphone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbPass, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbUser2))
                .addGap(15, 15, 15)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(faculry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbPass1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbUser3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbPass2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbUser4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Fathername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbPass3, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLogin1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(update)
                    .addComponent(nic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(panelLogin1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lb, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lb, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void FathernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FathernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FathernameActionPerformed

    private void dateEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateEActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dateEActionPerformed

    private void faculryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_faculryActionPerformed

    }//GEN-LAST:event_faculryActionPerformed

    private void updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateActionPerformed
       nematullah.service.StudentService.Student s = new nematullah.service.StudentService.Student();
       s.room = rnamber.getText();
       s.firstName = fname.getText();
       s.lastName = lname.getText();
       s.fatherName = Fathername.getText();
       s.nic = nic.getText();
       s.relativePhone = txtphone.getText();
       s.faculty = faculry.getSelectedItem() == null ? "" : faculry.getSelectedItem().toString();
       s.entryDate = dateE.getText();
       s.exitDate = dateExit.getText();
       try {
           // Changing the room here is a transfer: the capacity of the new room is checked by the service.
           nematullah.service.StudentService.update(Integer.parseInt(StudentForm.student_id), s);
           JOptionPane.showMessageDialog(this, "Changes Successfully Done");
           Window window = SwingUtilities.getWindowAncestor(this);
           if (window != null) {
               window.dispose();
           }
           StudentForm.loadData();
       } catch (nematullah.service.AppException e) {
           JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot save", JOptionPane.WARNING_MESSAGE);
       }
    }//GEN-LAST:event_updateActionPerformed

    private void rnamberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rnamberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rnamberActionPerformed

    private void lnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lnameActionPerformed

    private void dateExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateExitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dateExitActionPerformed

    private void fnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fnameActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Fathername;
    private javax.swing.JTextField dateE;
    private javax.swing.JTextField dateExit;
    private javax.swing.JComboBox<String> faculry;
    private javax.swing.JTextField fname;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lb;
    private javax.swing.JLabel lbPass;
    private javax.swing.JLabel lbPass1;
    private javax.swing.JLabel lbPass2;
    private javax.swing.JLabel lbPass3;
    private javax.swing.JLabel lbUser;
    private javax.swing.JLabel lbUser1;
    private javax.swing.JLabel lbUser2;
    private javax.swing.JLabel lbUser3;
    private javax.swing.JLabel lbUser4;
    private javax.swing.JTextField lname;
    private javax.swing.JFormattedTextField nic;
    private nematullah.application.form.PanelLogin panelLogin1;
    private javax.swing.JTextField rnamber;
    private javax.swing.JFormattedTextField txtphone;
    private javax.swing.JButton update;
    // End of variables declaration//GEN-END:variables
}
