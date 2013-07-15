package com.zuhlke.aztec.selenium.common;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SubjectTerm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class AztecAbstractTest {

	protected static final Integer WAIT_FOR_EMAIL = 5000;	
	protected static final String MONEY_PROP_FILE = "money.properties";
	protected static final String MANAGER_PROP_FILE = "manager.properties";

	protected static WebDriver driver;
	
	protected void findByIdAndClick(String id){
		WebElement elem = driver.findElement(By.id(id));
		elem.click();
	}
	
	protected void findByNameAndClick(String name){
		WebElement elem = driver.findElement(By.name(name));
		elem.click();
	}
	
	protected void findByXpathAndClick(String xpath){
		WebElement elem = driver.findElement(By.xpath(xpath));
		elem.click();
	}
	
	protected void findByLinkTextAndClick(String linkText){
		WebElement elem = driver.findElement(By.linkText(linkText));
		elem.click();
	}
	
	protected void findByIdAndSendKeys(String id, String keys){
		WebElement elem = driver.findElement(By.id(id));
		elem.clear();
		elem.sendKeys(keys);
	}
	
	protected void findByNameAndSendKeys(String name, String keys){
		WebElement elem = driver.findElement(By.name(name));
		elem.clear();
		elem.sendKeys(keys);
	}
	
	protected void findByXpathAndSendKeys(String xpath, String keys){
		WebElement elem = driver.findElement(By.xpath(xpath));
		elem.clear();
		elem.sendKeys(keys);
	}
	
	protected void findByIdAndSelectByVisibleText(String id, String visibleText){
		new Select(driver.findElement(By.id(id))).selectByVisibleText(visibleText);
	}
	
	protected String getVerificationLink(String email, String password, String subject) throws Exception{

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");

		Session session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", email, password);

		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_WRITE);

		Message[] messages = folder.search(new SubjectTerm(subject), folder.getMessages());
		boolean isMailFound = false;
		Message mail= null;

		//Search for unread mail 
		//This is to avoid using the mail for which registration is already done
		for (Message msg : messages) {
			if (!msg.isSet(Flags.Flag.SEEN)) {
				mail = msg;
				isMailFound = true;
			}
		}		

		String verificationLink = "";
		//Test fails if no unread mail was found from God
		if (!isMailFound) {
			throw new Exception("Could not find new mail :-(");
		//Read the content of mail and launch registration URL                
		} else {
			String content = "";
			if (mail.getContent() instanceof Multipart){
				Multipart multipart = (Multipart) mail.getContent();
				for (int i=0; i < multipart.getCount(); i++){
					BodyPart bodyPart = multipart.getBodyPart(i);
					if (bodyPart.getDisposition() != null && "ATTACHMENT".equalsIgnoreCase(bodyPart.getDisposition())){
						//Ignore, this is a file
					}
					else {
						content = content.concat(bodyPart.getContent().toString()); 
					}
				}
			}
			else {
				content = mail.getContent().toString();
			}
			
			Document doc = Jsoup.parse(content);
			verificationLink = doc.getElementsByClass("content").select("a[name=confirmationlink]").attr("href");
                         
		}
		
		return verificationLink;

	
	}
	
}
