package com.zuhlke.aztec.selenium.money;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.zuhlke.aztec.selenium.common.AztecAbstractTest;

public class SellerRegistrationTest extends AztecAbstractTest{

	private static final String REGNUMBER_PROP_NAME = "registrationNumber";
	private static final String PROP_DIR = "data";
	private static Logger logger = Logger.getLogger(SellerRegistrationTest.class.getCanonicalName());
		
	private String moneyBaseUrl = "https://qa.aztecmoney.com/";
	private String managerBaseUrl = "https://qa.aztecmanager.com/";

	private static String email = "";
	private static String emailPw = "";
	private static String subject = "";
	private String pw = "password1";

	@BeforeClass
	public static void classSetUp() throws ConfigurationException{
		//Driver setup
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);	
		//Open properties file
		PropertiesConfiguration moneyConfig = new PropertiesConfiguration(PROP_DIR + "/" +MONEY_PROP_FILE);
		//Global variables setup		
		email = moneyConfig.getProperty("email").toString();
		emailPw = moneyConfig.getProperty("pw").toString();
		subject = moneyConfig.getProperty("subject").toString();		
	}

	@Test
	//Seller's Registration - First Step
	public void sellerRegistrationFirstStepTest() throws Exception {					
		//Variables needed for this test
		String registrationNumber = getRegistrationNumberForFirstStep();
		String emailWithSuffix = getEmailWithSuffix(registrationNumber);
		logger.info("Running test case to create seller with email: " + emailWithSuffix);		
		//Start test
		driver.get(moneyBaseUrl); 
		this.findByXpathAndClick("//a[contains(@href,'Registration/Primary') and text()='SIGN UP']");
		this.findByIdAndSendKeys("EmailAddress", emailWithSuffix);
		this.findByIdAndSendKeys("Password", pw);
		this.findByIdAndSendKeys("RetypePassword", pw);
		this.findByIdAndSendKeys("SecurityAnswer", "whatever");
		this.findByIdAndSendKeys("FirstName", "Emma" + registrationNumber);
		this.findByIdAndSendKeys("LastName", "Holmes" + registrationNumber);
		this.findByIdAndSelectByVisibleText("BirthDay", "13");
		this.findByIdAndSelectByVisibleText("BirthMonth", "Nov");
		this.findByIdAndSelectByVisibleText("BirthYear", "1952");
		this.findByIdAndSelectByVisibleText("JobFunction", "CEO");
		this.findByIdAndSendKeys("JobTitle", "CEO");
		this.findByIdAndSendKeys("TelephoneCountryCode", "+44");
		this.findByIdAndSendKeys("TelephonePhoneNumber", "07777777777");
		this.findByIdAndSendKeys("MobilePhoneCountryCode", "+44");
		this.findByIdAndSendKeys("MobilePhonePhoneNumber", "07777777777");
		this.findByIdAndSendKeys("FaxCountryCode", "+44");
		this.findByIdAndSendKeys("FaxPhoneNumber", "07777777777");
		this.findByIdAndSelectByVisibleText("PreferredLanguage", "English");
		this.findByIdAndSelectByVisibleText("CountryOfOperation", "United Kingdom");
		this.findByNameAndClick("Next");
		this.findByXpathAndClick("//input[@type='button' and @value='BACK HOME']");
		//Increment registration number for future executions of this test
		incrementRegistrationNumber();
		//Check if driver is in home page (it means we pressed the BACK HOME button after registering a new seller)
		Assert.assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Your Business\\. Your Exports\\. Your Aztec Money\\.[\\s\\S]*$"));
		logger.info("Seller account " + emailWithSuffix + " was created in Aztec Money.");					
	}
	
	@Test
	//Seller's Registration - Second Step
	//NOTE: this test only works using zred network since it is not possible to connect to Gmail using javax.mail library through the proxy
	//Verifies recently created account using the link sent by email
	public void sellerRegistrationSecondStepTest() throws Exception {
		//Variables needed for this test
		String registrationNumber = getRegistrationNumber();
		String emailWithSuffix = getEmailWithSuffix(registrationNumber);		
		//Wait for email to be received
		Thread.sleep(WAIT_FOR_EMAIL);		
		//Go to the verify account link
		logger.info("Connecting to mail server for account " + email + " to retrieve the verification link");
		String verificationLink = this.getVerificationLink(email, emailPw, subject);
		logger.info("Verification link for email " + emailWithSuffix + " is: " + verificationLink );
		driver.get(verificationLink);		
		this.findByIdAndSendKeys("EmailAddress", emailWithSuffix);//Enter email address		
		this.findByIdAndSendKeys("Password", pw);//Enter password
		this.findByNameAndClick("Login");//Click login button
		logger.info("Account " + emailWithSuffix + " was verified.");
		Thread.sleep(5000);//Wait for registration page load		
		//Registration - Required documents
		this.findByIdAndClick("getStarted");
		Thread.sleep(5000);		
		//Registration - Company details
		this.createOrganisation(registrationNumber, "Asda1", true);
		this.createOrganisation(registrationNumber, "Asda2", false);
		Thread.sleep(5000);		
		//Registration - Summary and disclosure
		this.findByXpathAndClick("//input[@id='TradeFinance_HasLetterOfCredit' and @value='True']");
		this.findByXpathAndClick("//input[@id='TradeFinance_HasFactoring' and @value='False']");
		this.findByXpathAndClick("//input[@id='TradeFinance_HasInvoiceDiscounting' and @value='False']");
		this.findByXpathAndClick("//input[@id='TradeFinance_HasWorkingCapitalLoans' and  @value='True']");
		this.findByXpathAndClick("//input[@id='TradeFinance_HasAuctionExchange' and  @value='False']");
		this.findByIdAndClick("UploadDocumentsNowFax");
		this.findByIdAndClick("TermsAndConditionsAccepted");
		this.findByNameAndClick("Next");
		Thread.sleep(5000);
		//Registration - Fax Documents
		this.findByNameAndClick("Next");
		Thread.sleep(5000);
		//Go home
		this.findByXpathAndClick("//input[@type='button' and @value='BACK HOME']");
		Assert.assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Your Business\\. Your Exports\\. Your Aztec Money\\.[\\s\\S]*$"));
		logger.info("Seller completed the registration details for account " + emailWithSuffix);	
	}
	
	@Test
	//Seller's Registration - Third Step
	public void sellerRegistrationThirdStepTest() throws Exception {
		//Variables needed for this test
		String registrationNumber = getRegistrationNumber();
		String emailWithSuffix = getEmailWithSuffix(registrationNumber);
		//Manager login details
		PropertiesConfiguration managerConfig = new PropertiesConfiguration(PROP_DIR + "/" +MANAGER_PROP_FILE);
		String managerEmail = managerConfig.getProperty("email").toString();
		String managerPassword = managerConfig.getProperty("pw").toString();		
		//Manager login
		logger.info("Manager will accept the new seller " + emailWithSuffix);
		driver.get(managerBaseUrl);
		this.findByIdAndSendKeys("EmailAddress", managerEmail);
		this.findByIdAndSendKeys("Password", managerPassword);
		this.findByNameAndClick("Login");
		Thread.sleep(5000);
		//Go to seller registration
		this.findByLinkTextAndClick("Seller Registration");
		Thread.sleep(5000);
		//Search for seller created previously
		this.findByXpathAndSendKeys("//input[@type='text' and @name='q']", "(" + registrationNumber + ")");
		this.findByXpathAndClick("//form/input[@value='Search']");
		this.findByXpathAndClick("(//div[@class='entry'])[1]/div/a");
		//Check and edit registration details
		this.findByXpathAndClick("//span[contains(text(),'Registration details')]");
		Thread.sleep(5000);
		WebElement elem = driver.findElement(By.xpath("(//td[@class='secondColumn'])[1]"));
		Assert.assertEquals(emailWithSuffix,elem.getText());
		this.findByXpathAndClick("//a[contains(@href,'EditPersonalDetails')]");
		Thread.sleep(5000);
		this.findByIdAndSendKeys("SellerFee", "1.00");
		this.findByXpathAndClick("//input[@type='submit' and @value='SUBMIT']");
		Thread.sleep(5000);
		//Check and edit company details
		this.findByXpathAndClick("//span[contains(text(),'Company details')]");
		Thread.sleep(5000);
		this.findByXpathAndClick("//a[contains(@href,'EditOrganisation')]");
		Thread.sleep(5000);
		String newBIC = "New BIC";
		String newContactPerson = "Andrew Robinson";
		this.findByIdAndSendKeys("PrimaryBank_Accounts_0__AccountBicSwift", newBIC);
		this.findByIdAndSendKeys("ContactPerson", newContactPerson);
		this.findByXpathAndClick("//input[@type='submit' and @value='SUBMIT']");
		Thread.sleep(5000);
		this.findByXpathAndClick("//span[contains(text(),'Company details')]");
		//elem = driver.findElement(By.cssSelector("div.width100.float-left > span"));
		Thread.sleep(5000);
		elem = driver.findElement(By.xpath("(//table[contains(@class,'seller-company-details')]/tbody/tr/td[@class='secondColumn'])[12]"));
		Assert.assertEquals(newContactPerson, elem.getText());
		elem = driver.findElement(By.xpath("(//table[@id='bank-details-approve']/tbody/tr/td[@class='forthColumn'])[3]"));
		Assert.assertEquals(newBIC, elem.getText());
		this.findByXpathAndClick("//span[contains(text(),'Company details')]");
		//elem = driver.findElement(By.cssSelector("div.width100.float-left > span"));
		//Add documents
		this.uploadFile("resources/testFile.jpg", "Applicant photo ID (Passport, Drivers License, ID Card)", "Public");
		this.uploadFile("resources/testFile.jpg", "Other", "Private");
		this.uploadFile("resources/testFile.jpg", "Premises Lease", "Public");
		this.uploadFile("resources/testFile.jpg", "Company Accounts", "Public");
		//Add note
		this.findByIdAndSendKeys("noteMessage", "This is a test note");
		this.findByXpathAndClick("//input[@type='submit' and @value='ADD NOTE']");
		Thread.sleep(5000);
	    //Approve seller
		this.findByXpathAndClick("//a[contains(@href, '/Seller/Accept')]");
	}
	
	//"resources/testFile.jpg"
	private void uploadFile(String fileLocation, String documentType, String privacy) throws Exception{
		this.findByXpathAndClick("//span[contains(text(), 'Documents')]");
		Thread.sleep(5000);
		this.findByXpathAndClick("//a[contains(@href,'/Seller/AddDocument')]");
		//elem = driver.findElement(By.className("invoice-details-add-more"));
		Thread.sleep(5000);
		File file = new File(fileLocation);
		driver.findElement(By.id("SellerDocument_File")).sendKeys(file.getAbsolutePath()); //Cannot use method since input file cannot accept the selenium clear command
		this.findByIdAndSelectByVisibleText("SellerDocument_DocumentType", documentType);
		this.findByIdAndSelectByVisibleText("SellerDocument_PrivacyType", privacy);	  
		this.findByXpathAndClick("//input[@type='submit' and @value='SUBMIT']");
	    Thread.sleep(15000);
	}

	private String getEmailWithSuffix(String registrationNumber) throws ConfigurationException{		
		String emailWithSuffix = email.substring(0, email.indexOf('@')) + "+" + registrationNumber + email.substring(email.indexOf('@'));
		return emailWithSuffix;
	}
	
	private String getRegistrationNumberForFirstStep() throws ConfigurationException{
		PropertiesConfiguration moneyConfig = new PropertiesConfiguration(PROP_DIR + "/" +MONEY_PROP_FILE);
		return moneyConfig.getProperty(REGNUMBER_PROP_NAME).toString();
	}
	
	private String getRegistrationNumber() throws ConfigurationException{
		PropertiesConfiguration moneyConfig = new PropertiesConfiguration(PROP_DIR + "/" +MONEY_PROP_FILE);
		Integer regNumber = Integer.parseInt(moneyConfig.getProperty(REGNUMBER_PROP_NAME).toString())-1;
		return regNumber.toString();
	}
	
	private static void incrementRegistrationNumber() throws ConfigurationException{
		PropertiesConfiguration config = new PropertiesConfiguration(PROP_DIR + "/" +MONEY_PROP_FILE);
		String registrationNumber = config.getProperty(REGNUMBER_PROP_NAME).toString();
		config.setProperty(REGNUMBER_PROP_NAME, Integer.parseInt(registrationNumber)+1);
		config.save();
	}
	
	private void createOrganisation(String registrationNumber, String orgName, Boolean addAdditionalOrganisation){
		
		String suffix = " (" + registrationNumber + ")";		
		
		//Organisation details
		this.findByIdAndSendKeys("Name", orgName + suffix);
		this.findByIdAndSelectByVisibleText("CountryId", "United Kingdom");
		this.findByIdAndSendKeys("RegistrationNumber", orgName + "12345" + suffix);
		this.findByIdAndSendKeys("TaxNumber", orgName + "12345" + suffix);
		this.findByIdAndSendKeys("BuildingNumberAndStreet", orgName + " Central Offices");
		this.findByIdAndSendKeys("BuildingNumberAndStreet2", "30, Supermarket Road");
		this.findByIdAndSendKeys("City", "London");
		this.findByIdAndSendKeys("State", "London");
		this.findByIdAndSendKeys("PostCode", "E1 4BN");
		this.findByIdAndSendKeys("TelephoneCountryCode", "+44");
		this.findByIdAndSendKeys("TelephonePhoneNumber", "07777777777");
		this.findByIdAndSendKeys("FaxCountryCode", "+44");
		this.findByIdAndSendKeys("FaxPhoneNumber", "07777777777");
		this.findByIdAndSendKeys("ContactPerson", "Thomas Cook" + suffix);
		this.findByIdAndClick("HeadOfficeSameAsMyOrganisation");
			    
	    //Beneficial owners
		this.findByLinkTextAndClick("Add a beneficial owner");
	    this.addBeneficialOwner(suffix, 0);
	    this.addBeneficialOwner(suffix, 1);
		
	    //Secondary contacts
		this.findByLinkTextAndClick("Enter Details");
	    this.findByIdAndSendKeys("SecondaryContact1_EmailAddress", "secondaryContacts1_" + orgName.replace(" ", "") + "+" + registrationNumber + "@gmail.com");
	    this.findByIdAndSelectByVisibleText("SecondaryContact1_PositionId", "Assistant");
	    this.findByIdAndSendKeys("SecondaryContact2_EmailAddress", "secondaryContacts2_" + orgName.replace(" ", "") + "+" + registrationNumber + "@gmail.com");
	    this.findByIdAndSelectByVisibleText("SecondaryContact2_PositionId", "CFO");
	    this.findByIdAndSendKeys("SecondaryContact3_EmailAddress", "secondaryContacts3_" + orgName.replace(" ", "") + "+" + registrationNumber + "@gmail.com");
	    this.findByIdAndSelectByVisibleText("SecondaryContact3_PositionId", "COO");
	    
	    //Bank account
		this.findByLinkTextAndClick("Enter Details");//same as above because when secondary contacts are entered, the link is no longer called "Enter Details". It becomes a 'Hide details' link
	    this.addBankAccount("Primary", suffix);
	    this.addBankAccount("Secondary", suffix);    	
	    if (addAdditionalOrganisation) this.findByIdAndClick("AddAdditionalOrganisation");
	    this.findByIdAndClick("submitCompanyDetails");
	}
	
	private void addBeneficialOwner(String suffix, int i){		
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__Name","BeneficialOwner" + i + suffix);
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__OwnershipStake","3");
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__AddressLine1","BeneficialOwner" + i + " Address Line1");
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__AddressLine2","BeneficialOwner" + i + " Address Line2");
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__City","BeneficialOwner" + i +" Town");
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__State","BeneficialOwner" + i + " Region");
		this.findByIdAndSelectByVisibleText("BeneficialOwners_" + i +"__CountryId","United Kingdom");
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__CountryCode","+44");
		this.findByIdAndSendKeys("BeneficialOwners_" + i +"__Telephone","07777777777");
	}
	
	private void addBankAccount(String bankType, String suffix){		
		this.findByIdAndSendKeys(bankType + "Bank_BankName", bankType + "Bank" + suffix);
		this.findByIdAndSelectByVisibleText(bankType + "Bank_BankCountryId", "United Kingdom");
		this.findByNameAndSendKeys(bankType + "Bank.BankPhoneCountryCode", "+44");
		this.findByNameAndSendKeys(bankType + "Bank.BankPhonePhoneNumber", "07777777777");
		this.findByNameAndSendKeys(bankType + "Bank.BankFaxCountryCode", "+44");
		this.findByNameAndSendKeys(bankType + "Bank.BankFaxPhoneNumber", "07777777777");
		this.findByIdAndSendKeys(bankType + "Bank_BankBuildingNumberAndStreet", "Bank " + suffix + " Address Line 1");
		this.findByIdAndSendKeys(bankType + "Bank_BankBuildingNumberAndStreet2","Bank " + suffix + " Address Line 2");
		this.findByIdAndSendKeys(bankType + "Bank_BankCity", "Bank " + suffix + " Town");
		this.findByIdAndSendKeys(bankType + "Bank_BankState", "Bank " + suffix + " State");
		this.findByIdAndSendKeys(bankType + "Bank_BankPostCode", "Bank " + suffix + " Postcode");
		this.findByIdAndSendKeys(bankType + "Bank_Accounts_0__AccountOwner", "Account Owner " + bankType + suffix);
		this.findByIdAndSelectByVisibleText(bankType + "Bank_Accounts_0__AccountTypeId", "Personal");
		this.findByIdAndSendKeys(bankType + "Bank_Accounts_0__AccountNumber", "4587548745");
		this.findByIdAndSendKeys(bankType + "Bank_Accounts_0__AccountSortCode", "45-84-97");
		this.findByIdAndSendKeys(bankType + "Bank_Accounts_0__AccountIban", "GB29 RBOS 6016 1331 9268 19");
		this.findByIdAndSendKeys(bankType + "Bank_Accounts_0__AccountBicSwift", "BIC");
		this.findByIdAndSendKeys(bankType + "Bank_Accounts_0__AccountAbaRoutingNumber", "ABA");
		this.findByXpathAndClick("//input[@id='"+ bankType +"Bank_Accounts_0__AccountAreYouAccountOwnerSelection' and @value='No']");
		this.findByXpathAndClick("//input[@id='"+ bankType +"Bank_Accounts_0__AccountAcceptInternationalPaymentsSelection' and @value='No']");
		this.findByXpathAndClick("//input[@id='"+ bankType +"Bank_Accounts_0__AccountLimitsForeignTransferSizeSelection' and @value='No']");
		this.findByXpathAndClick("//input[@id='"+ bankType +"Bank_Accounts_0__AccountDomesticCurrencyRestrictionsSelection' and @value='No']");
	}

}
