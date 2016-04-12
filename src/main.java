import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import java.awt.Color; 
import java.awt.Font; 
import java.awt.Graphics; 
import java.awt.Graphics2D; 
import java.awt.print.PageFormat; 
import java.awt.print.Printable; 
import java.awt.print.PrinterException; 
import javax.print.Doc; 
import javax.print.DocFlavor; 
import javax.print.DocPrintJob; 
import javax.print.PrintException; 
import javax.print.PrintService; 
import javax.print.PrintServiceLookup; 
import javax.print.SimpleDoc; 
import javax.print.attribute.DocAttributeSet; 
import javax.print.attribute.HashDocAttributeSet; 
import javax.print.attribute.HashPrintRequestAttributeSet; 
import javax.print.attribute.PrintRequestAttributeSet; 
import javax.print.attribute.standard.MediaSizeName; 
import javax.swing.JPasswordField; 

class LocatePrint implements Printable { 
    private int PAGES = 0; 
    private int pagelimit = 10000; 
    
    public void setpagelimit(int page) {
    	pagelimit=page;
    }
    
    private ArrayList<String> strarr=new ArrayList();;

    /* 
     * Graphic指明打印的图形环境；PageFormat指明打印页格式（页面大小以点为计量单位， 
     * 1点为1英寸的1/72，1英寸为25.4毫米。A4纸大致为595×842点）；page指明页号 
     */ 
    public int print(Graphics gp, PageFormat pf, int page) 
            throws PrinterException { 
        Graphics2D g2 = (Graphics2D) gp; 
        g2.setPaint(Color.black); // 设置打印颜色为黑色 
        if (page >= PAGES) // 当打印页号大于需要打印的总页数时，打印工作结束 
            return Printable.NO_SUCH_PAGE; 
        g2.translate(pf.getImageableX(), pf.getImageableY());// 转换坐标，确定打印边界 
        Font font = new Font("微软雅黑", Font.PLAIN, 9);// 创建字体 
        g2.setFont(font); 
        // 打印当前页文本 
        //int printFontCount = printStr.length();// 打印字数 
        int printFontSize = font.getSize()+1;// Font 的磅值大小 

        float printMX = 10;
        float printMY = 10;
        
        int i;
        for(i=80*page;i<strarr.size() && i<=80*page+79;i++){
        	g2.drawString(strarr.get(i), printMX , printMY + printFontSize*(i-80*page)); // 具体打印每一行文本，同时走纸移位 
        }
        
        String tmpStr="第"+(page+1)+"页";
        g2.drawString(tmpStr,595 / 2- (tmpStr.length() * printFontSize / 2),printMY + printFontSize*81-4);
            
        return Printable.PAGE_EXISTS; // 存在打印页时，继续打印工作 
    }

    // 打印内容到指定位置 
    public int printContent(String printStr){ 
    	
    	strarr.clear();
    	
        if (printStr != null && printStr.length() > 0) // 当打印内容不为空时 
        {
        	printStr=printStr.replace("\t", "    ");
        	String[] arr=printStr.split("\n");
        	
        	for(int i=0;i<arr.length;i++){
        		if(arr[i].length()==0) strarr.add("");
        		else{
        			int j=0,curnum=135;
        			while(j*curnum<arr[i].length()){
        				strarr.add(arr[i].substring(j*curnum, j*curnum+curnum<arr[i].length()?j*curnum+curnum:arr[i].length()));
        				j++;
        			}
        		}
        	}
        	
            PAGES = (int) Math.ceil(strarr.size()/80.0); // 获取打印总页数 
            
            if(PAGES>pagelimit){//不打印
	            	return 2;
            }

            // 指定打印输出格式 
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE; 
            // 定位默认的打印服务 
            PrintService printService = PrintServiceLookup 
                    .lookupDefaultPrintService(); 
            // 创建打印作业 
            DocPrintJob job = printService.createPrintJob(); 
            // 设置打印属性 
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet(); 
            // 设置纸张大小,也可以新建MediaSize类来自定义大小 
            pras.add(MediaSizeName.ISO_A4); 
            DocAttributeSet das = new HashDocAttributeSet(); 
            // 指定打印内容 
            Doc doc = new SimpleDoc(this, flavor, das); 
            // 不显示打印对话框，直接进行打印工作 
            try { 
                job.print(doc, pras); // 进行每一页的具体打印操作 
                return 1;
            } catch (PrintException pe) { 
                pe.printStackTrace(); 
                return 0;
            } 
        } else { 
            // 如果打印内容为空时，提示用户打印将取消 
            JOptionPane.showConfirmDialog(null, 
                    "Sorry, Printer Job is Empty, Print Cancelled!", 
                    "Empty", JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.WARNING_MESSAGE); 
            return 0;
        } 
    } 
}

public class main extends JFrame {
	private JTextField IDtxt;
	protected HttpPost post;
	protected HttpResponse response;
	protected HttpClient client = new DefaultHttpClient();
	protected HttpEntity entity;
	protected String html;
	protected String cid;
	protected String IP;
	private JTextField IPtxt;
	JTextArea log = new JTextArea();
	
    LocatePrint printer = new LocatePrint(); 
    private JTextField usertxt;
    private JPasswordField pswtxt;
    private JTextField totaltxt;
    private JTextField modtxt;
    private JTextField pagetxt;
    private JTextField roomtxt;

	public class TimerTaskTest extends java.util.TimerTask{
		  
		@Override  
		public void run() { 
		   // TODO Auto-generated method stub  
			boolean err=false;
			//log.setText("getcodepending...\n"+log.getText());
			post = new HttpPost("http://"+IPtxt.getText()+"/admin/code_print.php");
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("getcodepending", "do"));
			nvps.add(new BasicNameValuePair("contest_id", cid));
			nvps.add(new BasicNameValuePair("total", totaltxt.getText()));
			nvps.add(new BasicNameValuePair("mod", modtxt.getText()));
			nvps.add(new BasicNameValuePair("room", roomtxt.getText()));
			
			post.setEntity(new UrlEncodedFormEntity(nvps,Charset.forName("utf8")));
			try {
				response = client.execute(post);
				entity = response.getEntity();
				html=EntityUtils.toString(entity, Charset.forName("utf8"));
				System.out.println(html);
				//log.setText("getcodepending success\n"+log.getText());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.setText("getcodepending failed..\n"+log.getText());
				err=true;
			}
			
			if(!err){
				String[] strarr=html.split("\n");
				
				for(int i=0;i<strarr.length-1;i+=5){
					String code=getcode(strarr[i]);
					
					String title="/*----------------------------\n"+"user="+(i+3<strarr.length?strarr[i+3]:"")+"\nnick="+strarr[i+1]+"\nseat="+(i+2<strarr.length?strarr[i+2]:"")
							+"\ntime="+(i+4<strarr.length?strarr[i+4]:"")+"\n----------------------------*/\n";
					System.out.println(title);
					int rtn=printer.printContent(title+code);
					if(rtn==1) {
						log.setText("id="+strarr[i]+" print success\n"+log.getText());
						setprinted(strarr[i],"1");
					}
					else if(rtn==2){
						log.setText("id="+strarr[i]+" print canceled\n"+log.getText());
						setprinted(strarr[i],"2");
					}
					else{
						JOptionPane.showMessageDialog(null,"title\n打印失败!", "系统信息", JOptionPane.ERROR_MESSAGE);
						log.setText("id="+strarr[i]+" print failed\n"+log.getText());
					}
				}
				
			}
		}
	}
	
	public void setprinted(String id,String val){
		//log.setText("setprinted id="+id+"...\n"+log.getText());
		post = new HttpPost("http://"+IPtxt.getText()+"/admin/code_print.php");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("updatcodeprinted", "do"));
		nvps.add(new BasicNameValuePair("id", id));
		nvps.add(new BasicNameValuePair("val", val));
		
		post.setEntity(new UrlEncodedFormEntity(nvps,Charset.forName("utf8")));
		try {
			response = client.execute(post);
			entity = response.getEntity();
			String tmpstr=EntityUtils.toString(entity, Charset.forName("utf8"));
			//log.setText("setprinted id="+id+" success\n"+log.getText());
			System.out.println(tmpstr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Boolean login(){
		log.setText("login...\n"+log.getText());
		post = new HttpPost("http://"+IPtxt.getText()+"/login.php");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("user_id", usertxt.getText()));
		nvps.add(new BasicNameValuePair("password", String.valueOf(pswtxt.getPassword())));
		
		post.setEntity(new UrlEncodedFormEntity(nvps,Charset.forName("utf8")));
		try {
			response = client.execute(post);
			entity = response.getEntity();
			String tmpstr=EntityUtils.toString(entity, Charset.forName("utf8"));
			System.out.println(tmpstr);
			if(tmpstr.contains("history.go(-2)")) return true;
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public String getcode(String id){
		log.setText("getcode id="+id+"...\n"+log.getText());
		post = new HttpPost("http://"+IPtxt.getText()+"/admin/code_print.php");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("getcodecontent", "do"));
		nvps.add(new BasicNameValuePair("id", id));
		
		post.setEntity(new UrlEncodedFormEntity(nvps,Charset.forName("utf8")));
		try {
			response = client.execute(post);
			entity = response.getEntity();
			String tmpstr=EntityUtils.toString(entity, Charset.forName("utf8"));
			System.out.println(tmpstr);
			log.setText("getcode id="+id+" success\n"+log.getText());
			return tmpstr;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					main frame = new main();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public main() {
		setResizable(false);
		setTitle("Printing Services");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 779, 662);
		
		JLabel lblNewLabel = new JLabel("Contest id:");
		
		IDtxt = new JTextField();
		IDtxt.setText("1061");
		IDtxt.setColumns(10);
		
		JButton btnNewButton = new JButton("start printing");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				IP=IPtxt.getText();
				cid=IDtxt.getText();
				if(login()==false){
					JOptionPane.showMessageDialog(null,"登录失败!", "系统信息", JOptionPane.ERROR_MESSAGE);
					log.setText("login failed\n"+log.getText());
				}
				else{
					IPtxt.setEnabled(false);
					IDtxt.setEnabled(false);
					usertxt.setEnabled(false);
					pswtxt.setEnabled(false);
					totaltxt.setEnabled(false);
					modtxt.setEnabled(false);
					pagetxt.setEnabled(false);
					roomtxt.setEnabled(false);
					btnNewButton.setEnabled(false);
					btnNewButton.setText("printing");
					log.setText("login success\n"+log.getText());
					
					printer.setpagelimit(Integer.parseInt(pagetxt.getText()));

					Timer timer = new Timer();  
					timer.schedule(new TimerTaskTest(), 1000, 5000);  	
				}
			}
		});
		
		JLabel lblNewLabel_1 = new JLabel("Server IP:");
		
		IPtxt = new JTextField();
		IPtxt.setText("acm.wust.edu.cn");
		IPtxt.setColumns(10);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { 
			super.windowClosing(e);  
		         try {
		             FileOutputStream out=new FileOutputStream("Faith Printing Services.txt");
		             PrintStream p=new PrintStream(out);
		             p.println(IPtxt.getText());
		             p.println(IDtxt.getText());
		             p.println(usertxt.getText());
		             p.println(totaltxt.getText());
		             p.println(modtxt.getText());
		             p.println(pagetxt.getText());
		             p.println(roomtxt.getText());
		             p.close();
		         } catch (FileNotFoundException e1) {
		             e1.printStackTrace();
		         }
			}  
			  
		}); 
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		JLabel lblNewLabel_2 = new JLabel("user:");
		
		usertxt = new JTextField();
		usertxt.setText("printer");
		usertxt.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("password:");
		
		pswtxt = new JPasswordField();
		
		JLabel lblNewLabel_4 = new JLabel("total:");
		
		totaltxt = new JTextField();
		totaltxt.setText("1");
		totaltxt.setColumns(10);
		
		JLabel lblNewLabel_5 = new JLabel("mod:");
		
		modtxt = new JTextField();
		modtxt.setText("0");
		modtxt.setColumns(10);
		
		JLabel lblNewLabel_6 = new JLabel("don't print when pages more than");
		
		pagetxt = new JTextField();
		pagetxt.setText("8");
		pagetxt.setColumns(10);
		
		JLabel lblNewLabel_7 = new JLabel("Machine Room:");
		
		roomtxt = new JTextField();
		roomtxt.setColumns(10);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(24)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblNewLabel_4)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(totaltxt, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(lblNewLabel_5)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(modtxt, 0, 0, Short.MAX_VALUE))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblNewLabel_2)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(usertxt, 170, 170, 170))
								.addGroup(groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblNewLabel_1)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(IPtxt, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)))
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(lblNewLabel)
										.addComponent(lblNewLabel_3, GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(IDtxt, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(lblNewLabel_7)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(roomtxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addComponent(pswtxt, GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblNewLabel_6)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(pagetxt, GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)))
							.addGap(19)
							.addComponent(btnNewButton)
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addGap(14))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(23)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 479, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_2)
								.addComponent(usertxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_3)
								.addComponent(pswtxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(43)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_4)
								.addComponent(totaltxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_5)
								.addComponent(modtxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_6)
								.addComponent(pagetxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
							.addComponent(IDtxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblNewLabel)
							.addComponent(IPtxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblNewLabel_1)
							.addComponent(lblNewLabel_7)
							.addComponent(roomtxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(14))
		);
		
		 File file=new File("Faith Printing Services.txt");
         if(!(!file.exists()||file.isDirectory()))
         {
             BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
             String temp=null;
             String sb="";
             try {
				temp=br.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				
			}
             while(temp!=null){
                 sb+=temp+"\n";
                 try {
					temp=br.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
             }
	         
             try{
		         System.out.println(sb);
		         String [] lxd=sb.split("\n");
		         IPtxt.setText(lxd[0]);
		         IDtxt.setText(lxd[1]);
		         usertxt.setText(lxd[2]);
		         totaltxt.setText(lxd[3]);
		         modtxt.setText(lxd[4]);
		         pagetxt.setText(lxd[5]);
		         roomtxt.setText(lxd[6]);
             }catch (Exception e){
            	 e.printStackTrace();
             }
         }
		
		log.setEditable(false);
		
		scrollPane.setViewportView(log);
		getContentPane().setLayout(groupLayout);
		
	}
}
