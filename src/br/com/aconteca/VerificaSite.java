package br.com.aconteca;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VerificaSite extends JFrame {

	private static final long serialVersionUID = 1L;
	private static String HTTP = "http://";
	private static String DS_OFFLINE = "OFFLINE";
	private static String DS_TIMEOUT = "TIMEOUT";
	private static int LARGURA_TELA = 450;
	private static int ALTURA_TELA = 100;

	@SuppressWarnings("static-access")
	public VerificaSite() throws MalformedURLException, IOException {
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		
		URL urlImagem = getClass().getResource("ajax-loader.gif");
		Image imagem = Toolkit.getDefaultToolkit().getImage(urlImagem);
		ImageIcon load = new ImageIcon(imagem);
		JLabel textoInformacaoExecucao = new JLabel("Aguarde, executando verificação...");
		textoInformacaoExecucao.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel loadLabel = new JLabel();
		loadLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		JTextField status = new JTextField();
		status.setSize(LARGURA_TELA, 40);
		status.setEditable(false);
		status.setForeground(Color.BLACK);
		status.setFont(new Font("monospaced", Font.BOLD, 10));
		status.setHorizontalAlignment(SwingConstants.LEFT);
		
		JFileChooser arquivoEscolhido = new JFileChooser();
		arquivoEscolhido.setDialogTitle("ESCOLHA ARQUIVO PARA LER");
	    FileNameExtensionFilter filterArquivoEscolhido = new FileNameExtensionFilter("TXT", "txt");
	    arquivoEscolhido.setFileFilter(filterArquivoEscolhido);
	    
	    JFileChooser arquivoGerado = new JFileChooser();
	    arquivoGerado.setDialogTitle("ESCOLHA NOME DO ARQUIVO E LUGAR ONDE SERÃO SALVO");
	    FileNameExtensionFilter filterArquivoGerado = new FileNameExtensionFilter("TXT", "txt");
	    arquivoGerado.setFileFilter(filterArquivoGerado);
	    
	    int abrirArquivoSelecionado = arquivoEscolhido.showOpenDialog(this);
	    int salvarArquivoSelecionado = arquivoGerado.showSaveDialog(this);
	    
	    if (abrirArquivoSelecionado == JFileChooser.APPROVE_OPTION && salvarArquivoSelecionado == JFileChooser.APPROVE_OPTION) {
	    	File arquivoComExtensao = null;
	 	    if (!arquivoGerado.getSelectedFile().getName().endsWith(".txt")) {
	 	    	arquivoComExtensao = new File(arquivoGerado.getSelectedFile() + ".txt");
	 	    } else {
	 	    	arquivoComExtensao = arquivoGerado.getSelectedFile();
	 	    }
	 	    
	 	    loadLabel.setIcon(load);
	 	    container.add(textoInformacaoExecucao, BorderLayout.PAGE_START);
	 	    container.add(loadLabel, BorderLayout.CENTER);
	 	    container.add(status, BorderLayout.SOUTH);
	    	setVisible(true);
			setTitle("Verificador de Sites Online");
			setLocationRelativeTo(null);
			setSize(LARGURA_TELA, ALTURA_TELA);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setResizable(false);
			
	    	File file = arquivoEscolhido.getSelectedFile();
			FileReader reader = new FileReader(file);
			BufferedReader conteudo = new BufferedReader(reader);
			
			System.setProperty("http.keepAlive", "false");
			
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			
			while(conteudo.ready()) {
				String site = conteudo.readLine();
				if (!site.equals("")) {
					URL url = new URL(HTTP + site);
					HttpURLConnection huc = (HttpURLConnection)url.openConnection();
					huc.setInstanceFollowRedirects(true);
					huc.setFollowRedirects(true);
					huc.setRequestMethod ("GET");
					huc.setRequestProperty("User-Agent", "Mozilla/4.76");
					try {
						status.setText("Site: " + url);
						huc.setConnectTimeout(30000);
						huc.setReadTimeout(20000);
						huc.connect();
						if (huc.getResponseCode() != HttpURLConnection.HTTP_OK) {
							salvarLogErros(url, arquivoComExtensao, DS_OFFLINE);
						}
					} catch (UnknownHostException e) {
						salvarLogErros(url, arquivoComExtensao, DS_OFFLINE);
						continue;
					} catch (SocketTimeoutException e) {
						salvarLogErros(url, arquivoComExtensao, DS_TIMEOUT);
						continue;
					} catch (Exception e) {
						salvarLogErros(url, arquivoComExtensao, e.getMessage());
						continue;
					}
					
				}
			}
			conteudo.close();
	    } else {
	    	System.exit(0);
	    }
	    
	    setVisible(false);
	    
	    JOptionPane.showMessageDialog(null, "Processo finalizado com sucesso!");
	    System.exit(0);
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		new VerificaSite();
	}
	
	public static void salvarLogErros(URL url, File arquivoGerado, String tipoErro) throws IOException {
		FileWriter fw = new FileWriter(arquivoGerado, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(url.toString() + " - Erro: " + tipoErro);
		bw.newLine();
		bw.close();
		fw.close();
	}
	
}