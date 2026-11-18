package nematullah.application.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import nemat.application.Application;
import nemat.application.form.other.AddCost;
import nemat.application.form.other.AddEmployee;
import nemat.application.form.other.AddSalary;
import nemat.application.form.other.AddUser;
import nemat.application.form.other.CostForm;
import nemat.application.form.other.EmployeeForm;
import nemat.application.form.other.FormDashboard;
import nemat.application.form.other.FormAdd;
import nemat.application.form.other.FormDelet;
import nemat.application.form.other.FormUpdate;
import nemat.application.form.other.PaymentForm;
import nemat.application.form.other.RoomNamber;
import nemat.application.form.other.Salaryform;
import nemat.application.form.other.Sitting;
import nemat.application.form.other.StudentForm;
import nemat.application.form.other.userForm;
import nemat.menu.Menu;
import nemat.menu.MenuAction;


public class MainForm extends JLayeredPane {

    public MainForm() {
        init();
    }

    private void init() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new MainFormLayout());
        menu = new Menu();
        panelBody = new JPanel(new BorderLayout());
        initMenuArrowIcon();
        menuButton.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:$Menu.button.background;"
                + "arc:999;"
                + "focusWidth:0;"
                + "borderWidth:0");
        menuButton.addActionListener((ActionEvent e) -> {
            setMenuFull(!menu.isMenuFull());
        });
        initMenuEvent();
        setLayer(menuButton, JLayeredPane.POPUP_LAYER);
        add(menuButton);
        add(menu);
        add(panelBody);
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        initMenuArrowIcon();
    }

    private void initMenuArrowIcon() {
        if (menuButton == null) {
            menuButton = new JButton();
        }
        String icon = (getComponentOrientation().isLeftToRight()) ? "menu_left.svg" : "menu_right.svg";
        menuButton.setIcon(new FlatSVGIcon("nemat/icon/svg/" + icon, 0.8f));
    }

    private void initMenuEvent() {
        menu.addMenuEvent((int index, int subIndex, MenuAction action) -> {
            // Application.mainForm.showForm(new DefaultForm("Form : " + index + " " + subIndex));
            if (index == 0) {
                Application.showForm(new FormDashboard());
            } 
            else if (index == 1) {
                    if(subIndex == 0){
                    Application.showForm(new StudentForm());
                    }
                     else if (subIndex == 1) {
                    Application.showForm(new FormAdd());
                    } else if (subIndex == 2) {
                    Application.showForm(new FormDelet());
                    }else if(subIndex==3){
                    Application.showForm(new FormUpdate());
                     }}
          else if(index == 2){
          Application.showForm(new RoomNamber());
          }
          else if(index==3){
          Application.showForm(new PaymentForm());
          }
          else if(index==4){
          if (subIndex == 1) {
              Application.showForm(new AddEmployee());
          } else {
              Application.showForm(new EmployeeForm());
          }
          }
          else if(index==5){
              if (subIndex == 1) {
              Application.showForm(new AddSalary());
          } else {
              Application.showForm(new Salaryform());
          }
          }
          else if(index==6){
              if (subIndex == 1) {
              Application.showForm(new AddCost());
          } else {
              Application.showForm(new CostForm());
          }
          }
          else if(index==7){
              if (!nematullah.service.Session.isAdmin()) {
              javax.swing.JOptionPane.showMessageDialog(null, "Only an Admin can manage users.");
              action.cancel();
              return;
          }
          if (subIndex == 1) {
              Application.showForm(new AddUser());
          } else {
              Application.showForm(new userForm());
          }
          }
           else if(index==8){
              Application.showForm(new Sitting());
          }
           else if (index == 9) {
            Application.logout();
            } else {
                action.cancel();
            }
        });
    }

    private void setMenuFull(boolean full) {
        String icon;
        if (getComponentOrientation().isLeftToRight()) {
            icon = (full) ? "menu_left.svg" : "menu_right.svg";
        } else {
            icon = (full) ? "menu_right.svg" : "menu_left.svg";
        }
        menuButton.setIcon(new FlatSVGIcon("nemat/icon/svg/" + icon, 0.8f));
        menu.setMenuFull(full);
        revalidate();
    }

    public void hideMenu() {
        menu.hideMenuItem();
    }

    public void showForm(Component component) {
        panelBody.removeAll();
        panelBody.add(component);
        panelBody.repaint();
        panelBody.revalidate();
    }

    public void setSelectedMenu(int index, int subIndex) {
        menu.setSelectedMenu(index, subIndex);
    }

    private Menu menu;
    private JPanel panelBody;
    private JButton menuButton;

    private class MainFormLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(5, 5);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(0, 0);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = UIScale.scale(parent.getInsets());
                int x = insets.left;
                int y = insets.top;
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);
                int menuWidth = UIScale.scale(menu.isMenuFull() ? menu.getMenuMaxWidth() : menu.getMenuMinWidth());
                int menuX = ltr ? x : x + width - menuWidth;
                menu.setBounds(menuX, y, menuWidth, height);
                int menuButtonWidth = menuButton.getPreferredSize().width;
                int menuButtonHeight = menuButton.getPreferredSize().height;
                int menubX;
                if (ltr) {
                    menubX = (int) (x + menuWidth - (menuButtonWidth * (menu.isMenuFull() ? 0.5f : 0.3f)));
                } else {
                    menubX = (int) (menuX - (menuButtonWidth * (menu.isMenuFull() ? 0.5f : 0.7f)));
                }
                menuButton.setBounds(menubX, UIScale.scale(30), menuButtonWidth, menuButtonHeight);
                int gap = UIScale.scale(5);
                int bodyWidth = width - menuWidth - gap;
                int bodyHeight = height;
                int bodyx = ltr ? (x + menuWidth + gap) : x;
                int bodyy = y;
                panelBody.setBounds(bodyx, bodyy, bodyWidth, bodyHeight);
            }
        }
    }
}
