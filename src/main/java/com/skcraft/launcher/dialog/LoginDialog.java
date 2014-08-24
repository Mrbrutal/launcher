/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import static com.skcraft.launcher.util.SharedLocale._;

/**
 * The login dialog.
 */
public class LoginDialog extends JDialog {

    private final Launcher launcher;
    @Getter private final AccountList accounts;
    @Getter private Session session;

    private final HeaderPanel header = new HeaderPanel();
    private final JComboBox idCombo = new JComboBox();
    private final JPasswordField passwordText = new JPasswordField();
    private final JCheckBox rememberIdCheck = new JCheckBox(_("login.rememberId"));
    private final JCheckBox rememberPassCheck = new JCheckBox(_("login.rememberPassword"));
    private final JButton loginButton = new JButton(_("login.login"));
    private final LinkButton recoverButton = new LinkButton(_("login.recoverAccount"));
    private final JButton offlineButton = new JButton(_("login.playOffline"));
    private final JButton cancelButton = new JButton(_("button.cancel"));
    private final FormPanel formPanel = new FormPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);

    private final JLabel password = new JLabel(_("login.idPassword"));
    private final JLabel username = new JLabel(_("login.password"));

    private final CustomJLabel login = new CustomJLabel("launch");
    private final CustomJLabel cancel = new CustomJLabel("launch");
    private final CustomJLabel forgot = new CustomJLabel("launch");
    private final CustomJLabel offline = new CustomJLabel("refresh");

    private final CustomJLabel x = new CustomJLabel("x");
    private JLabel name;
    private Point initialClick;

    /**
     * Create a new login dialog.
     *
     * @param owner the owner
     * @param launcher the launcher
     */
    public LoginDialog(Window owner, @NonNull Launcher launcher) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.launcher = launcher;
        this.accounts = launcher.getAccounts();
        name = new JLabel(_("launcher.title", launcher.getVersion()));

        setTitle(_("login.title"));
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 0));
        setResizable(false);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(0xff86493b)));

        pack();
        setLocationRelativeTo(owner);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                removeListeners();
                dispose();
            }
        });
    }

    private void removeListeners() {
        idCombo.setModel(new DefaultComboBoxModel());
    }

    private void initComponents() {
        idCombo.setModel(getAccounts());
        updateSelection();

        x.setSize(30,20);

        name.setForeground(Color.white);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setFont(name.getFont().deriveFont(12.0f));
        name.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 10));

        header.add(name);
        header.add(Box.createHorizontalGlue());
        header.add(x);
        add(header, BorderLayout.NORTH);

        login.setSize(82, 28);
        login.setText(_("login.login"));
        login.setHorizontalTextPosition(JLabel.CENTER);
        login.setVerticalTextPosition(JLabel.CENTER);
        login.setFont(login.getFont().deriveFont(Font.BOLD));

        cancel.setSize(82, 28);
        cancel.setText(_("button.cancel"));
        cancel.setHorizontalTextPosition(JLabel.CENTER);
        cancel.setVerticalTextPosition(JLabel.CENTER);

        forgot.setSize(82, 28);
        forgot.setText(_("login.recoverAccount"));
        forgot.setHorizontalTextPosition(JLabel.CENTER);
        forgot.setVerticalTextPosition(JLabel.CENTER);
        forgot.setCursor(new Cursor(Cursor.HAND_CURSOR));

        offline.setSize(82, 28);
        offline.setText(_("login.playOffline"));
        offline.setHorizontalTextPosition(JLabel.CENTER);
        offline.setVerticalTextPosition(JLabel.CENTER);
        offline.setFont(offline.getFont().deriveFont(Font.BOLD));

        rememberIdCheck.setBorder(BorderFactory.createEmptyBorder());
        rememberIdCheck.setOpaque(false);
        rememberIdCheck.setForeground(Color.white);
        rememberPassCheck.setBorder(BorderFactory.createEmptyBorder());
        rememberPassCheck.setOpaque(false);
        rememberPassCheck.setForeground(Color.white);
        idCombo.setEditable(true);
        idCombo.getEditor().selectAll();

        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD));

        password.setForeground(Color.white);
        username.setForeground(Color.white);

        formPanel.addRow(password, idCombo);
        formPanel.addRow(username, passwordText);
        formPanel.addRow(new JLabel(), rememberIdCheck);
        formPanel.addRow(new JLabel(), rememberPassCheck);
        formPanel.setForeground(Color.white);
        formPanel.setBackground(new Color(0xff86493b));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));

        //buttonsPanel.addElement(recoverButton);
        buttonsPanel.addElement(forgot);
        buttonsPanel.addGlue();
        //buttonsPanel.addElement(loginButton);
        //buttonsPanel.addElement(cancelButton);
        if (launcher.getConfig().isOfflineEnabled()) {
            //buttonsPanel.addElement(offlineButton);
            buttonsPanel.addElement(offline);
            //buttonsPanel.addElement(Box.createHorizontalStrut(2));
        }
        buttonsPanel.addElement(login);
        //buttonsPanel.addElement(cancel);

        buttonsPanel.setBackground(new Color(0xff1c1c1e));

        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(loginButton);

        passwordText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        idCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelection();
            }
        });

        idCombo.getEditor().getEditorComponent().addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent e) {
                popupManageMenu(e.getComponent(), e.getX(), e.getY());
            }
        });

        recoverButton.addActionListener(
                ActionListeners.openURL(recoverButton, launcher.getProperties().getProperty("resetPasswordUrl")));

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prepareLogin();
            }
        });

        offlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setResult(new OfflineSession(launcher.getProperties().getProperty("offlinePlayerName")));
                removeListeners();
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeListeners();
                dispose();
            }
        });

        rememberPassCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rememberPassCheck.isSelected()) {
                    rememberIdCheck.setSelected(true);
                }
            }
        });

        rememberIdCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!rememberIdCheck.isSelected()) {
                    rememberPassCheck.setSelected(false);
                }
            }
        });

        login.addMouseListener(new MouseListener() {
            boolean entered = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                prepareLogin();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_pressed_" + login.name + ".png"));
                login.setIcon(icon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ImageIcon icon;
                if(entered) {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + login.name + ".png"));
                }
                else {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + login.name + ".png"));
                }
                login.setIcon(icon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + login.name + ".png"));
                login.setIcon(icon);
                entered = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + login.name + ".png"));
                login.setIcon(icon);
                entered = false;
            }
        });

        cancel.addMouseListener(new MouseListener() {
            boolean entered = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                removeListeners();
                dispose();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_pressed_" + cancel.name + ".png"));
                cancel.setIcon(icon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ImageIcon icon;
                if(entered) {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + cancel.name + ".png"));
                }
                else {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + cancel.name + ".png"));
                }
                cancel.setIcon(icon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + cancel.name + ".png"));
                cancel.setIcon(icon);
                entered = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + cancel.name + ".png"));
                cancel.setIcon(icon);
                entered = false;
            }
        });

        forgot.addMouseListener(new MouseListener() {
            boolean entered = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(launcher.getProperties().getProperty("resetPasswordUrl")));
                }catch(Exception ex) {
                    //It looks like there's a problem
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_pressed_" + forgot.name + ".png"));
                forgot.setIcon(icon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ImageIcon icon;
                if(entered) {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + forgot.name + ".png"));
                }
                else {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + forgot.name + ".png"));
                }
                forgot.setIcon(icon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + forgot.name + ".png"));
                forgot.setIcon(icon);
                entered = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + forgot.name + ".png"));
                forgot.setIcon(icon);
                entered = false;
            }
        });

        x.addMouseListener(new MouseListener() {
            boolean entered = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                removeListeners();
                dispose();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + x.name + "_pressed.png"));
                x.setIcon(icon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ImageIcon icon;
                if(entered) {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + x.name + "_hover.png"));
                }
                else {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + x.name + "_default.png"));
                }
                x.setIcon(icon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + x.name + "_hover.png"));
                x.setIcon(icon);
                entered = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + x.name + "_default.png"));
                x.setIcon(icon);
                entered = false;
            }
        });

        offline.addMouseListener(new MouseListener() {
            boolean entered = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                setResult(new OfflineSession(launcher.getProperties().getProperty("offlinePlayerName")));
                removeListeners();
                dispose();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_pressed_" + offline.name + ".png"));
                offline.setIcon(icon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ImageIcon icon;
                if(entered) {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + offline.name + ".png"));
                }
                else {
                    icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + offline.name + ".png"));
                }
                offline.setIcon(icon);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_hover_" + offline.name + ".png"));
                offline.setIcon(icon);
                entered = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + offline.name + ".png"));
                offline.setIcon(icon);
                entered = false;
            }
        });

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // get location of Window
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
                int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                setLocation(X, Y);
            }
        });
    }

    private void popupManageMenu(Component component, int x, int y) {
        Object selected = idCombo.getSelectedItem();
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if (selected != null && selected instanceof Account) {
            final Account account = (Account) selected;

            menuItem = new JMenuItem(_("login.forgetUser"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    accounts.remove(account);
                    Persistence.commitAndForget(accounts);
                }
            });
            popup.add(menuItem);

            if (!Strings.isNullOrEmpty(account.getPassword())) {
                menuItem = new JMenuItem(_("login.forgetPassword"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        account.setPassword(null);
                        Persistence.commitAndForget(accounts);
                    }
                });
                popup.add(menuItem);
            }
        }

        menuItem = new JMenuItem(_("login.forgetAllPasswords"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SwingHelper.confirmDialog(LoginDialog.this,
                        _("login.confirmForgetAllPasswords"),
                        _("login.forgetAllPasswordsTitle"))) {
                    accounts.forgetPasswords();
                    Persistence.commitAndForget(accounts);
                }
            }
        });
        popup.add(menuItem);

        popup.show(component, x, y);
    }

    private void updateSelection() {
        Object selected = idCombo.getSelectedItem();

        if (selected != null && selected instanceof Account) {
            Account account = (Account) selected;
            String password = account.getPassword();

            rememberIdCheck.setSelected(true);
            if (!Strings.isNullOrEmpty(password)) {
                rememberPassCheck.setSelected(true);
                passwordText.setText(password);
            } else {
                rememberPassCheck.setSelected(false);
            }
        } else {
            passwordText.setText("");
            rememberIdCheck.setSelected(true);
            rememberPassCheck.setSelected(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void prepareLogin() {
        Object selected = idCombo.getSelectedItem();

        if (selected != null && selected instanceof Account) {
            Account account = (Account) selected;
            String password = passwordText.getText();

            if (password == null || password.isEmpty()) {
                SwingHelper.showErrorDialog(this, _("login.noPasswordError"), _("login.noPasswordTitle"));
            } else {
                if (rememberPassCheck.isSelected()) {
                    account.setPassword(password);
                } else {
                    account.setPassword(null);
                }

                if (rememberIdCheck.isSelected()) {
                    accounts.add(account);
                } else {
                    accounts.remove(account);
                }

                account.setLastUsed(new Date());

                Persistence.commitAndForget(accounts);

                attemptLogin(account, password);
            }
        } else {
            SwingHelper.showErrorDialog(this, _("login.noLoginError"), _("login.noLoginTitle"));
        }
    }

    private void attemptLogin(Account account, String password) {
        LoginCallable callable = new LoginCallable(account, password);
        ObservableFuture<Session> future = new ObservableFuture<Session>(
                launcher.getExecutor().submit(callable), callable);

        Futures.addCallback(future, new FutureCallback<Session>() {
            @Override
            public void onSuccess(Session result) {
                setResult(result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(this, future, _("login.loggingInTitle"), _("login.loggingInStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void setResult(Session session) {
        this.session = session;
        removeListeners();
        dispose();
    }

    public static Session showLoginRequest(Window owner, Launcher launcher) {
        LoginDialog dialog = new LoginDialog(owner, launcher);
        dialog.setVisible(true);
        return dialog.getSession();
    }

    private class LoginCallable implements Callable<Session>,ProgressObservable {
        private final Account account;
        private final String password;

        private LoginCallable(Account account, String password) {
            this.account = account;
            this.password = password;
        }

        @Override
        public Session call() throws AuthenticationException, IOException, InterruptedException {
            LoginService service = launcher.getLoginService();
            List<? extends Session> identities = service.login(launcher.getProperties().getProperty("agentName"), account.getId(), password);

            // The list of identities (profiles in Mojang terms) corresponds to whether the account
            // owns the game, so we need to check that
            if (identities.size() > 0) {
                // Set offline enabled flag to true
                Configuration config = launcher.getConfig();
                if (!config.isOfflineEnabled()) {
                    config.setOfflineEnabled(true);
                    Persistence.commitAndForget(config);
                }

                Persistence.commitAndForget(getAccounts());
                return identities.get(0);
            } else {
                throw new AuthenticationException("Minecraft not owned", _("login.minecraftNotOwnedError"));
            }
        }

        @Override
        public double getProgress() {
            return -1;
        }

        @Override
        public String getStatus() {
            return _("login.loggingInStatus");
        }
    }
}