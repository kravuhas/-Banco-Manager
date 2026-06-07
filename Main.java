import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

// ══════════════════════════════════════════════════════════
// MODELO: PESSOA BASE
// ══════════════════════════════════════════════════════════
class Person {
    protected String name;
    protected String email;
    protected String phone;

    public String getName()  { return name;  }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}

// ══════════════════════════════════════════════════════════
// MODELO: CONTA BANCÁRIA
// ══════════════════════════════════════════════════════════
class BankAccount extends Person {
    private int    accno;
    private String cpf;
    private String type;
    private double balance;
    private ArrayList<String> history = new ArrayList<>();

    public BankAccount(int accno, String name, String cpf, String email,
                       String phone, String type, double balance) {
        this.accno   = accno;
        this.name    = name;
        this.cpf     = cpf;
        this.email   = email;
        this.phone   = phone;
        this.type    = type;
        this.balance = balance;
        addHistory("Conta criada com saldo inicial R$ " + String.format("%.2f", balance));
    }

    public int    getAccno()   { return accno; }
    public String getCpf()     { return cpf;   }
    public String getType()    { return type;  }
    public double getBalance() { return balance; }
    public ArrayList<String> getHistory() { return history; }

    private void addHistory(String entry) {
        history.add("[" + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + "] " + entry);
    }

    public void deposit(double amount) {
        balance += amount;
        addHistory("Depósito: +R$ " + String.format("%.2f", amount) + " | Saldo: R$ " + String.format("%.2f", balance));
    }

    public boolean withdraw(double amount) {
        if (amount > balance) return false;
        balance -= amount;
        addHistory("Saque: -R$ " + String.format("%.2f", amount) + " | Saldo: R$ " + String.format("%.2f", balance));
        return true;
    }

    public boolean sendPix(double amount, BankAccount dest) {
        if (amount > balance) return false;
        balance -= amount;
        dest.balance += amount;
        addHistory("Pix enviado: -R$ " + String.format("%.2f", amount) + " → " + dest.getName());
        dest.history.add("[" + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + "] "
            + "Pix recebido: +R$ " + String.format("%.2f", amount) + " de " + this.name);
        return true;
    }

    @Override
    public String toString() {
        return String.format("#%d | %s | %s | R$ %.2f", accno, name, type, balance);
    }
}

// ══════════════════════════════════════════════════════════
// SERVIÇO DE EMAIL (Gmail SMTP)
// ══════════════════════════════════════════════════════════
class EmailService {

    // ── Configurações — troque pelos seus dados ───────────────
    private static final String SMTP_USER = "seuemail@gmail.com";
    private static final String SMTP_PASS = "sua_senha_de_app";  // Senha de App do Google
    // ─────────────────────────────────────────────────────────

    public static boolean send(String toEmail, String toName, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SMTP_USER, "Banco Manager"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // Email em HTML bonito
            String html = buildEmailHtml(toName, body);
            msg.setContent(html, "text/html; charset=UTF-8");

            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            return false;
        }
    }

    private static String buildEmailHtml(String name, String body) {
        return """
            <html><body style="font-family:Segoe UI,sans-serif;background:#f0f4f8;margin:0;padding:20px">
            <div style="max-width:520px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.1)">
              <div style="background:linear-gradient(135deg,#0a1428,#003264);padding:32px;text-align:center">
                <h1 style="color:#fff;margin:0;font-size:22px">💳 Banco Manager</h1>
                <p style="color:#90c8ff;margin:6px 0 0;font-size:13px">Sistema de Gerenciamento Bancário</p>
              </div>
              <div style="padding:28px 32px">
                <p style="color:#334;font-size:15px">Olá, <strong>%s</strong>!</p>
                <div style="background:#f7faff;border-left:4px solid #0078d4;border-radius:4px;padding:16px;margin:16px 0;color:#223;font-size:14px;line-height:1.7">%s</div>
                <p style="color:#888;font-size:12px;margin-top:24px">Este é um email automático do Banco Manager.<br>Não responda esta mensagem.</p>
              </div>
              <div style="background:#f0f4f8;padding:16px;text-align:center">
                <p style="color:#aaa;font-size:11px;margin:0">© 2025 Banco Manager · Todos os direitos reservados</p>
              </div>
            </div></body></html>
            """.formatted(name, body.replace("\n", "<br>"));
    }
}

// ══════════════════════════════════════════════════════════
// JANELA: ENVIAR EMAIL PERSONALIZADO
// ══════════════════════════════════════════════════════════
class SendEmailDialog extends JDialog {

    public SendEmailDialog(JFrame parent, BankAccount acc) {
        super(parent, "Enviar Email — " + acc.getName(), true);
        setSize(520, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(new Color(12, 22, 45));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Título
        JLabel title = new JLabel("✉  Enviar Email para " + acc.getName());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        // Campos
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(new Color(12, 22, 45));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Email destino
        JTextField toField = darkField(acc.getEmail());
        // Assunto
        JTextField subjectField = darkField("Comunicado — Banco Manager");
        // Mensagem
        JTextArea bodyArea = new JTextArea(6, 1);
        bodyArea.setBackground(new Color(20, 40, 80));
        bodyArea.setForeground(Color.WHITE);
        bodyArea.setCaretColor(Color.WHITE);
        bodyArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bodyArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setText("Prezado(a) " + acc.getName() + ",\n\nInformamos que sua conta #"
            + acc.getAccno() + " está ativa.\n\nSaldo atual: R$ "
            + String.format("%.2f", acc.getBalance()) + "\n\nAtenciosamente,\nEquipe Banco Manager");

        addRow(fields, gbc, 0, "Destinatário (email):", toField);
        addRow(fields, gbc, 1, "Assunto:", subjectField);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel("Mensagem:");
        lbl.setForeground(new Color(160, 200, 255));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fields.add(lbl, gbc);
        gbc.gridy = 3; gbc.gridwidth = 2;
        fields.add(new JScrollPane(bodyArea), gbc);

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(new Color(12, 22, 45));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton cancelBtn = darkButton("Cancelar", new Color(80, 80, 100));
        JButton sendBtn   = darkButton("📨  Enviar Email", new Color(0, 120, 215));

        cancelBtn.addActionListener(e -> dispose());
        sendBtn.addActionListener(e -> {
            String to      = toField.getText().trim();
            String subject = subjectField.getText().trim();
            String body    = bodyArea.getText().trim();

            if (to.isEmpty() || subject.isEmpty() || body.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            sendBtn.setText("Enviando...");
            sendBtn.setEnabled(false);

            // Envia em thread separada para não travar a UI
            new Thread(() -> {
                boolean ok = EmailService.send(to, acc.getName(), subject, body);
                SwingUtilities.invokeLater(() -> {
                    if (ok) {
                        JOptionPane.showMessageDialog(this,
                            "✅ Email enviado com sucesso para:\n" + to,
                            "Email Enviado", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "❌ Falha ao enviar email.\n\nVerifique:\n" +
                            "• Sua senha de app do Gmail em EmailService.java\n" +
                            "• Conexão com internet\n" +
                            "• Email de destino correto",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                        sendBtn.setText("📨  Enviar Email");
                        sendBtn.setEnabled(true);
                    }
                });
            }).start();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(sendBtn);

        panel.add(title,    BorderLayout.NORTH);
        panel.add(fields,   BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(panel);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row * 2; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(160, 200, 255));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lbl, gbc);
        gbc.gridy = row * 2 + 1; gbc.gridwidth = 2; gbc.weightx = 1;
        p.add(field, gbc);
    }

    private JTextField darkField(String text) {
        JTextField f = new JTextField(text);
        f.setBackground(new Color(20, 40, 80));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 200)),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return f;
    }

    private JButton darkButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 38));
        return b;
    }
}

// ══════════════════════════════════════════════════════════
// JANELA: HISTÓRICO DE TRANSAÇÕES
// ══════════════════════════════════════════════════════════
class HistoryDialog extends JDialog {
    public HistoryDialog(JFrame parent, BankAccount acc) {
        super(parent, "Histórico — " + acc.getName(), true);
        setSize(540, 400);
        setLocationRelativeTo(parent);

        JTextArea area = new JTextArea();
        area.setBackground(new Color(10, 20, 45));
        area.setForeground(new Color(0, 220, 150));
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        area.setEditable(false);

        StringBuilder sb = new StringBuilder("📋 EXTRATO — " + acc.getName() + "\n");
        sb.append("Conta #").append(acc.getAccno()).append(" | ").append(acc.getType()).append("\n");
        sb.append("─".repeat(52)).append("\n");
        for (String h : acc.getHistory()) sb.append(h).append("\n");
        sb.append("─".repeat(52)).append("\n");
        sb.append("Saldo atual: R$ ").append(String.format("%.2f", acc.getBalance()));
        area.setText(sb.toString());

        setContentPane(new JScrollPane(area));
    }
}

// ══════════════════════════════════════════════════════════
// TELA DE LOGIN
// ══════════════════════════════════════════════════════════
class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Banco Manager — Login");
        setSize(440, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(8, 16, 36), 0, getHeight(), new Color(0, 45, 95)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(50, 20, 10, 20));

        JLabel icon  = new JLabel("💳");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("BANCO MANAGER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sistema de Gerenciamento Bancário");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(130, 185, 255));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // Form
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(30, 50, 20, 50));

        JTextField userField = styledField("admin");
        JPasswordField passField = new JPasswordField();
        styleField(passField);

        JButton btn = new JButton("ENTRAR  →");
        btn.setBackground(new Color(0, 112, 200));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (userField.getText().equals("admin") && new String(passField.getPassword()).equals("1234")) {
                dispose();
                new MainBankFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos.\nUse: admin / 1234",
                    "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Enter key
        passField.addActionListener(e -> btn.doClick());

        form.add(fieldLabel("Usuário"));
        form.add(Box.createVerticalStrut(5));
        form.add(userField);
        form.add(Box.createVerticalStrut(14));
        form.add(fieldLabel("Senha"));
        form.add(Box.createVerticalStrut(5));
        form.add(passField);
        form.add(Box.createVerticalStrut(22));
        form.add(btn);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        JLabel hint = new JLabel("admin / 1234");
        hint.setForeground(new Color(90, 140, 190));
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.add(hint);

        main.add(header, BorderLayout.NORTH);
        main.add(form,   BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(160, 210, 255));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private JTextField styledField(String text) {
        JTextField f = new JTextField(text);
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(18, 35, 72));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 90, 180)),
            BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }
}

// ══════════════════════════════════════════════════════════
// PAINEL PRINCIPAL (Dashboard)
// ══════════════════════════════════════════════════════════
class MainBankFrame extends JFrame {

    private ArrayList<BankAccount> accounts = new ArrayList<>();
    private JTextArea outputArea;
    private JLabel statusBar;
    private int nextAccno = 1001;

    public MainBankFrame() {
        setTitle("Banco Manager — Painel Principal");
        setSize(960, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Contas demo
        accounts.add(new BankAccount(nextAccno++, "Ana Silva",    "111.222.333-44", "ana@email.com",    "(11)91111-1111", "Corrente",  15000.00));
        accounts.add(new BankAccount(nextAccno++, "Carlos Souza", "555.666.777-88", "carlos@email.com", "(11)92222-2222", "Poupança",   8500.50));
        accounts.add(new BankAccount(nextAccno++, "Maria Lima",   "999.888.777-66", "maria@email.com",  "(11)93333-3333", "Corrente",  32000.00));

        setLayout(new BorderLayout());

        // ── Sidebar ─────────────────────────────────────────────────────
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(5, 12, 30), 0, getHeight(), new Color(0, 35, 72)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 12, 24, 12));

        JLabel logo = new JLabel("  💳 BANCO MGR");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0, 70, 140));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        // Grupos de menu
        addMenuGroup(sidebar, "CONTAS",
            new String[]{"Nova Conta", "Listar Contas", "Histórico"});
        sidebar.add(Box.createVerticalStrut(10));
        addMenuGroup(sidebar, "TRANSAÇÕES",
            new String[]{"Depositar", "Sacar", "Enviar Pix", "Ver Saldo"});
        sidebar.add(Box.createVerticalStrut(10));
        addMenuGroup(sidebar, "COMUNICAÇÃO",
            new String[]{"Enviar Email"});
        sidebar.add(Box.createVerticalGlue());
        addMenuGroup(sidebar, "",
            new String[]{"Sair"});

        // ── Área principal ────────────────────────────────────────────────
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(12, 22, 48));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0, 25, 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        JLabel topTitle = new JLabel("Painel Principal");
        topTitle.setForeground(Color.WHITE);
        topTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel topDate = new JLabel(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        topDate.setForeground(new Color(100, 160, 220));
        topDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topBar.add(topTitle, BorderLayout.WEST);
        topBar.add(topDate,  BorderLayout.EAST);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setBackground(new Color(12, 22, 48));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 10, 18));
        statsPanel.add(statCard("Contas Ativas",  String.valueOf(accounts.size()), new Color(0, 100, 200)));
        double totalBal = accounts.stream().mapToDouble(BankAccount::getBalance).sum();
        statsPanel.add(statCard("Saldo Total", "R$ " + String.format("%,.2f", totalBal), new Color(0, 150, 100)));
        statsPanel.add(statCard("Tipo Mais Comum", "Corrente", new Color(150, 80, 0)));

        // Output
        outputArea = new JTextArea();
        outputArea.setBackground(new Color(8, 16, 38));
        outputArea.setForeground(new Color(0, 215, 145));
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        outputArea.setEditable(false);
        outputArea.setText("══════════════════════════════════════════\n"
            + "  BANCO MANAGER — Sistema Iniciado\n"
            + "══════════════════════════════════════════\n"
            + "  Contas carregadas: " + accounts.size() + "\n"
            + "  Saldo total: R$ " + String.format("%,.2f", totalBal) + "\n"
            + "══════════════════════════════════════════\n");

        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(8, 16, 38));

        // Status bar
        statusBar = new JLabel("  Pronto");
        statusBar.setForeground(new Color(100, 180, 255));
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.setBackground(new Color(0, 18, 45));
        statusBar.setOpaque(true);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        content.add(topBar,     BorderLayout.NORTH);
        content.add(statsPanel, BorderLayout.BEFORE_FIRST_LINE); // workaround: use wrapper
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(new Color(12, 22, 48));
        centerWrapper.add(statsPanel, BorderLayout.NORTH);
        centerWrapper.add(scroll,     BorderLayout.CENTER);
        content.add(centerWrapper, BorderLayout.CENTER);
        content.add(statusBar,     BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private void addMenuGroup(JPanel panel, String groupTitle, String[] items) {
        if (!groupTitle.isEmpty()) {
            JLabel grp = new JLabel("  " + groupTitle);
            grp.setForeground(new Color(80, 130, 200));
            grp.setFont(new Font("Segoe UI", Font.BOLD, 10));
            grp.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(grp);
            panel.add(Box.createVerticalStrut(4));
        }
        for (String item : items) {
            JButton btn = new JButton("  " + item);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBackground(new Color(0, 0, 0, 0));
            btn.setForeground(new Color(200, 225, 255));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setOpaque(false);
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setOpaque(true);
                    btn.setBackground(new Color(0, 70, 150));
                    btn.setForeground(Color.WHITE);
                }
                public void mouseExited(MouseEvent e) {
                    btn.setOpaque(false);
                    btn.setForeground(new Color(200, 225, 255));
                }
            });
            btn.addActionListener(e -> handleMenu(item));
            panel.add(btn);
            panel.add(Box.createVerticalStrut(2));
        }
    }

    private JPanel statCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(16, 30, 62));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(120, 170, 220));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel val = new JLabel(value);
        val.setForeground(Color.WHITE);
        val.setFont(new Font("Segoe UI", Font.BOLD, 18));
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private void handleMenu(String action) {
        setStatus("Executando: " + action + "...");
        switch (action) {
            case "Nova Conta"    -> createAccount();
            case "Listar Contas" -> listAccounts();
            case "Histórico"     -> showHistory();
            case "Depositar"     -> deposit();
            case "Sacar"         -> withdraw();
            case "Enviar Pix"    -> sendPix();
            case "Ver Saldo"     -> checkBalance();
            case "Enviar Email"  -> sendEmail();
            case "Sair"          -> { dispose(); new LoginFrame().setVisible(true); }
        }
        setStatus("Pronto");
    }

    private void createAccount() {
        JTextField nameField  = new JTextField();
        JTextField cpfField   = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Corrente", "Poupança"});
        JTextField balField   = new JTextField("0.00");

        Object[] fields = {
            "Nome completo:", nameField,
            "CPF:", cpfField,
            "Email:", emailField,
            "Telefone:", phoneField,
            "Tipo de conta:", typeBox,
            "Depósito inicial (R$):", balField
        };

        int r = JOptionPane.showConfirmDialog(this, fields, "Nova Conta", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            try {
                double bal = Double.parseDouble(balField.getText().replace(",", "."));
                BankAccount acc = new BankAccount(nextAccno++,
                    nameField.getText(), cpfField.getText(),
                    emailField.getText(), phoneField.getText(),
                    (String) typeBox.getSelectedItem(), bal);
                accounts.add(acc);
                print("✅ Conta criada!\n" + acc);
            } catch (NumberFormatException e) { print("❌ Valor inválido."); }
        }
    }

    private void deposit() {
        BankAccount acc = selectAccount("Conta para depósito:");
        if (acc == null) return;
        String val = JOptionPane.showInputDialog(this, "Valor (R$):");
        if (val != null) try {
            double v = Double.parseDouble(val.replace(",", "."));
            acc.deposit(v);
            print("✅ Depósito de R$ " + String.format("%.2f", v) + " → " + acc.getName()
                + "\nNovo saldo: R$ " + String.format("%.2f", acc.getBalance()));
        } catch (NumberFormatException e) { print("❌ Valor inválido."); }
    }

    private void withdraw() {
        BankAccount acc = selectAccount("Conta para saque:");
        if (acc == null) return;
        String val = JOptionPane.showInputDialog(this, "Valor (R$):");
        if (val != null) try {
            double v = Double.parseDouble(val.replace(",", "."));
            if (acc.withdraw(v))
                print("✅ Saque de R$ " + String.format("%.2f", v) + " — " + acc.getName()
                    + "\nNovo saldo: R$ " + String.format("%.2f", acc.getBalance()));
            else print("❌ Saldo insuficiente. Atual: R$ " + String.format("%.2f", acc.getBalance()));
        } catch (NumberFormatException e) { print("❌ Valor inválido."); }
    }

    private void sendPix() {
        BankAccount from = selectAccount("Conta REMETENTE:");
        if (from == null) return;
        BankAccount to = selectAccount("Conta DESTINO:");
        if (to == null || from.getAccno() == to.getAccno()) { print("❌ Contas iguais."); return; }
        String val = JOptionPane.showInputDialog(this, "Valor do Pix (R$):");
        if (val != null) try {
            double v = Double.parseDouble(val.replace(",", "."));
            if (from.sendPix(v, to))
                print("✅ Pix R$ " + String.format("%.2f", v) + "\n"
                    + from.getName() + " → " + to.getName()
                    + "\nSaldo remetente: R$ " + String.format("%.2f", from.getBalance()));
            else print("❌ Saldo insuficiente para Pix.");
        } catch (NumberFormatException e) { print("❌ Valor inválido."); }
    }

    private void checkBalance() {
        BankAccount acc = selectAccount("Selecione a conta:");
        if (acc != null)
            print("💰 " + acc.getName() + " — Conta #" + acc.getAccno()
                + "\nSaldo: R$ " + String.format("%.2f", acc.getBalance()));
    }

    private void listAccounts() {
        if (accounts.isEmpty()) { print("Nenhuma conta cadastrada."); return; }
        StringBuilder sb = new StringBuilder("📋 CONTAS CADASTRADAS\n");
        sb.append("─".repeat(55)).append("\n");
        for (BankAccount a : accounts) {
            sb.append(a).append("\n");
            sb.append("   Email: ").append(a.getEmail())
              .append(" | Tel: ").append(a.getPhone()).append("\n");
        }
        sb.append("─".repeat(55)).append("\n");
        sb.append("Total: ").append(accounts.size()).append(" conta(s)");
        print(sb.toString());
    }

    private void showHistory() {
        BankAccount acc = selectAccount("Ver histórico de qual conta:");
        if (acc != null) new HistoryDialog(this, acc).setVisible(true);
    }

    private void sendEmail() {
        BankAccount acc = selectAccount("Enviar email para qual cliente:");
        if (acc == null) return;
        if (acc.getEmail() == null || acc.getEmail().isEmpty()) {
            print("❌ Esta conta não tem email cadastrado.");
            return;
        }
        new SendEmailDialog(this, acc).setVisible(true);
    }

    private BankAccount selectAccount(String msg) {
        if (accounts.isEmpty()) { print("Nenhuma conta cadastrada."); return null; }
        String[] opts = accounts.stream().map(BankAccount::toString).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, msg, "Selecionar Conta",
            JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (sel == null) return null;
        return accounts.stream().filter(a -> a.toString().equals(sel)).findFirst().orElse(null);
    }

    private void print(String msg) {
        outputArea.append("\n" + msg + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void setStatus(String msg) {
        statusBar.setText("  " + msg);
    }
}

// ══════════════════════════════════════════════════════════
// PONTO DE ENTRADA
// ══════════════════════════════════════════════════════════
public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
