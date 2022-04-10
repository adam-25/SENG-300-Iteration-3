package seng300.software.GUI;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.lsmr.selfcheckout.Banknote;
import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.BarcodedItem;
import org.lsmr.selfcheckout.Card;
import org.lsmr.selfcheckout.Coin;
import org.lsmr.selfcheckout.InvalidArgumentSimulationException;
import org.lsmr.selfcheckout.Item;
import org.lsmr.selfcheckout.PLUCodedItem;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.devices.DisabledException;
import org.lsmr.selfcheckout.devices.OverloadException;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.external.ProductDatabases;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

import seng300.software.MembersProgramStub;
import seng300.software.MembershipCard;
import seng300.software.PLUCodedWeightProduct;
import seng300.software.PayWithCoin;
import seng300.software.SelfCheckoutSystemLogic;
import seng300.software.GUI.ProductLookupPanel.ResultsPanel;
import seng300.software.exceptions.ProductNotFoundException;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;

public class CustomerGui extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7637105711156584933L;
	
	private StationUnavailablePanel unavailablePanel;
	private StationReadyPanel readyPanel;
	private CustomerCheckoutPanel checkoutPanel;
	private ProductLookupPanel lookupPanel;
	private CustomerPaymentPanel paymentPanel;
	private EnterMembershipPanel membershipPanel;
	private CoinPaymentPanel payCoinPanel;
	private BanknotePaymentPanel payBanknotePanel;
	private CheckoutCompletePanel checkoutCompletePanel;
	private PaymentFailedPanel paymentFailedPanel;
	private BaggingAreaPanel baggingAreaPanel;
	private EnterPlasticBagsPanel plasticBagsPanel;
	private RemoveItemLog removeItemLog;
	
	private SelfCheckoutSystemLogic logic;
	
	/**
	 * Create the panel.
	 */
	public CustomerGui(SelfCheckoutSystemLogic logic) 
	{
		setLayout(new CardLayout(0, 0));
		
		this.logic = logic;
		
		unavailablePanel = new StationUnavailablePanel();
		readyPanel = new StationReadyPanel();
		readyPanel.startButton.addActionListener(e -> displayCheckoutPanel());
		
		checkoutPanel = new CustomerCheckoutPanel();
		checkoutPanel.searchProductBtn.addActionListener(e -> displayProductLookupPanel());
		checkoutPanel.useOwnBagsBtn.addActionListener(e -> useOwnBagsClicked());
		checkoutPanel.checkoutBtn.addActionListener(e -> displayPlasticBagsPanel());
		checkoutPanel.pluEntryPinPad.padEnterBtn.addActionListener(e -> getPluCode());
		checkoutPanel.viewBaggingAreaBtn.addActionListener(e -> displayBaggingAreaPanel());
		checkoutPanel.removeItemBtn.addActionListener(e -> removeItemBtnClicked());
		
		lookupPanel = new ProductLookupPanel();
		lookupPanel.returnButton.addActionListener(e -> displayCheckoutPanel());
		for (KeyboardButton btn : lookupPanel.keyboardBtns)
		{
			btn.addActionListener(e -> push(btn));
		}
		
		paymentPanel = new CustomerPaymentPanel();
		paymentPanel.returnToCheckoutBtn.addActionListener(e -> returnToCheckoutClicked());
		paymentPanel.addMembershipBtn.addActionListener(e -> displayMembershipPanel());
		paymentPanel.payWithCoinBtn.addActionListener(e -> displayPayCoinPanel());
		paymentPanel.payWithCashBtn.addActionListener(e -> displayPayCashPanel());
		
		payCoinPanel = new CoinPaymentPanel();
		payCoinPanel.doneBtn.addActionListener(e -> displayPaymentPanel());
		payCoinPanel.dimeBtn.addActionListener(e -> payDime());
		payCoinPanel.loonieBtn.addActionListener(e -> payLoonie());
		payCoinPanel.nickelBtn.addActionListener(e -> payNickel());
		payCoinPanel.quarterBtn.addActionListener(e -> payQuarter());
		payCoinPanel.toonieBtn.addActionListener(e -> payToonie());
		
		payBanknotePanel = new BanknotePaymentPanel();
		payBanknotePanel.doneBtn.addActionListener(e -> displayPaymentPanel());
		payBanknotePanel.fiveBtn.addActionListener(e -> payFive());
		payBanknotePanel.hundredBtn.addActionListener(e -> payHundred());
		payBanknotePanel.fiftyBtn.addActionListener(e -> payFifty());
		payBanknotePanel.twentyBtn.addActionListener(e -> payTwenty());
		payBanknotePanel.tenBtn.addActionListener(e -> payTen());
		
		membershipPanel = new EnterMembershipPanel();
		membershipPanel.cancelBtn.addActionListener(e -> displayPaymentPanel());
		membershipPanel.pinPad.padEnterBtn.addActionListener(e -> addMembershipToCheckout(membershipPanel.pinPad.getValue()));

		baggingAreaPanel = new BaggingAreaPanel(new ArrayList<String>());
		baggingAreaPanel.returnButton.addActionListener(e -> displayCheckoutPanel());
		baggingAreaPanel.deleteButton.addActionListener(e -> removeItemfromBaggingClicked(baggingAreaPanel.getCurrentSelectedIndex()));
		
		plasticBagsPanel = new EnterPlasticBagsPanel();
		plasticBagsPanel.pinPad.padEnterBtn.addActionListener(e -> enterNumPlasticBags(Integer.parseInt(plasticBagsPanel.pinPad.getValue())));
		
		add(unavailablePanel);
		add(readyPanel);
		add(checkoutPanel);
		add(lookupPanel);
		add(paymentPanel);
		add(membershipPanel);
		add(payCoinPanel);
		add(payBanknotePanel);
		add(baggingAreaPanel);
		add(plasticBagsPanel);
		shutdown();
		
		
	}
	
	public void reset() // called between customers at end of checkout
	{
		unavailablePanel.setVisible(false);
		readyPanel.setVisible(true);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
	}
	
	public void startup()
	{
		logic.turnOnStation();
		readyPanel.setVisible(true);
		unavailablePanel.setVisible(false);
	}
	
	public void shutdown()
	{
		unavailablePanel.setVisible(true);
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
		logic.turnOffStation();
	}
	
	public void useOwnBagsClicked()
	{
		checkoutPanel.showAttendantNotifiedPanel();
//		logic.ownBagBlock();
//		checkoutPanel.showLogoPanel();
	}
	
	public void displayCheckoutPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(true);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
	}
	
	public void displayBaggingAreaPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(true);
		plasticBagsPanel.setVisible(false);
	}
	
	public void displayProductLookupPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(true);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
	}
	
	public void displayPlasticBagsPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(true);
	}
	
	public void displayPaymentPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(true);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
	}
	
	public void displayMembershipPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(true);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
	}
	
	public void displayPayCoinPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(true);
		payBanknotePanel.setVisible(false);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
	}
	
	public void displayPayCashPanel()
	{
		readyPanel.setVisible(false);
		checkoutPanel.setVisible(false);
		lookupPanel.setVisible(false);
		paymentPanel.setVisible(false);
		membershipPanel.setVisible(false);
		payCoinPanel.setVisible(false);
		payBanknotePanel.setVisible(true);
		baggingAreaPanel.setVisible(false);
		plasticBagsPanel.setVisible(false);
	}
	
	public void lookupProduct(String searchText)
	{
		if (!searchText.isEmpty())
		{
			List<PLUCodedProduct> results = logic.productLookUp(searchText);
			List<LookupResultButton> btns = new ArrayList<>();
			for (PLUCodedProduct p : results)
			{
				LookupResultButton btn = new LookupResultButton(p);
				btn.addActionListener(e -> {
					try {
						addPluProductToCart(btn.getProduct().getPLUCode());
					} catch (ProductNotFoundException ex) {
						// This should never execute.
					}
				});
				btns.add(btn);
			}
			lookupPanel.displayProducts(btns);
		}
		// ignore empty searches
	}
	
	private void addPluProductToCart(PriceLookupCode code) throws ProductNotFoundException
	{
		if (ProductDatabases.PLU_PRODUCT_DATABASE.containsKey(code))
		{
			// Create random plucoded product for testing
			double maxScaleWeight = logic.station.scanningArea.getWeightLimit();
			Random rand = new Random();
			PLUCodedItem item = new PLUCodedItem(code, rand.nextDouble() * maxScaleWeight);
			// add product to cart (no exception should ever be thrown)
			logic.getCart().addPLUCodedProductToCart(code, item.getWeight());
			displayCheckoutPanel();
		}
		else
		{
			throw new ProductNotFoundException();
		}
	}
	
	
	public void scanRandomItem()
	{
		// get random barcode from product database
		Random rand = new Random();
		int index = rand.nextInt(ProductDatabases.BARCODED_PRODUCT_DATABASE.size());
		Barcode code = ((Barcode[])ProductDatabases.BARCODED_PRODUCT_DATABASE.keySet().toArray())[index];
		BarcodedProduct p = ProductDatabases.BARCODED_PRODUCT_DATABASE.get(code);
		BarcodedItem item = new BarcodedItem(code, p.getExpectedWeight());
		// scan until product added successfully
		int oldSize = this.logic.getCart().getProducts().size();
		while(this.logic.getCart().getProducts().size() <= oldSize)
		{
			this.logic.station.mainScanner.scan(item);
		}
	}
	
	private void push(KeyboardButton btn)
	{
		KeyboardKey key = btn.getKey();
		String searchText = lookupPanel.getSearchText();
		if (key == KeyboardKey.BACK)
		{
			if (!searchText.isEmpty())
			{
				searchText = searchText.substring(0, searchText.length() - 1);
				lookupPanel.setSearchText(searchText);
			}
			// ignore attempts to backspace when search field empty
		}
		else if (key == KeyboardKey.CLEAR)
		{
			if (!searchText.isEmpty())
			{
				lookupPanel.reset();
			}
			// ignore attempts to clear when search field empty
		}
		else if (key != KeyboardKey.ENTER)
		{
			searchText += key.getValue();
			lookupPanel.setSearchText(searchText);
		}
		lookupProduct(searchText);
	}
	
	private void getPluCode()
	{
		String value = checkoutPanel.pluEntryPinPad.getValue();
		if(!value.isEmpty())
		{
			try
			{
				PriceLookupCode code = new PriceLookupCode(value);
				checkoutPanel.hidePluEntryPanelErrorMsg();
				addPluProductToCart(code);
				checkoutPanel.pluEntryPinPad.clear();
				checkoutPanel.showLogoPanel();
			}
			catch(Exception e)
			{
				checkoutPanel.showPluEntryPanelErrorMsg();
				checkoutPanel.pluEntryPinPad.clear();
			}
		}
		// ignore empty searches
	}
	
	private void removeItemfromBaggingClicked(int index)
	{
		Product p = this.logic.getBaggedProducts().get(index);
		this.logic.selectItemToRemove(p); //should work for barcoded and plu coded products
		double weight;
		if (p instanceof BarcodedProduct){
		    weight = ((BarcodedProduct) p).getExpectedWeight();
		    this.logic.station.baggingArea.remove(new BarcodedItem(((BarcodedProduct)p).getBarcode(), weight));
		}
		else if (p instanceof PLUCodedWeightProduct){
		    weight = ((PLUCodedWeightProduct)p).getWeight();
		    this.logic.station.baggingArea.remove(new PLUCodedItem(((PLUCodedWeightProduct)p).getPLUCode(), weight));
		}
		//maybe a sleep?
		this.logic.returnToNormalBaggingOperation();
	}
	
	//places the last added to the cart?
	private void placeItem()
	{
		
	}
	
	private void addMembershipToCheckout(String input)
	{
		//TODO
	}
		
	private void enterNumPlasticBags(int numPlasticBags)
	{
		this.logic.getCart().addPlasticBags(numPlasticBags);
		this.logic.wantsToCheckout();
		displayPaymentPanel();
	}
	
	private void returnToCheckoutClicked()
	{
		this.logic.addItemAfterCheckoutStart();
		displayCheckoutPanel();
	}
	
	private void removeItemBtnClicked()
	{
		removeItemLog = new RemoveItemLog(this.logic.getCart().getProducts());
		JPanel panel = (JPanel)removeItemLog.getContentPane();
		checkoutPanel.setLeftPanel(panel);
	}
	
	//---Payment methods---
	
	private void payNickel()
	{
		BigDecimal value = new BigDecimal("0.05");
		Coin coin = new Coin(value);
		try {
			logic.station.coinSlot.accept(coin);
			logic.amountPaid = logic.amountPaid + coin.getValue().intValue();
		} catch (DisabledException e) {
			
			e.printStackTrace();
		} catch (OverloadException e) {
			
			e.printStackTrace();
		}
		
	}
	
	private void payDime()
	{
		BigDecimal value = new BigDecimal("0.10");
		Coin coin = new Coin(value);
		try {
			logic.station.coinSlot.accept(coin);
			logic.amountPaid = logic.amountPaid + coin.getValue().intValue();
		} catch (DisabledException e) {
			
			e.printStackTrace();
		} catch (OverloadException e) {
			
			e.printStackTrace();
		}
	}
	
	private void payQuarter()
	{
		BigDecimal value = new BigDecimal("0.25");
		Coin coin = new Coin(value);
		try {
			logic.station.coinSlot.accept(coin);
			logic.amountPaid = logic.amountPaid + coin.getValue().intValue();
		} catch (DisabledException e) {
			
			e.printStackTrace();
		} catch (OverloadException e) {
			
			e.printStackTrace();
		}
	}
	
	private void payLoonie()
	{
		BigDecimal value = new BigDecimal("1.00");
		Coin coin = new Coin(value);
		try {
			logic.station.coinSlot.accept(coin);
			logic.amountPaid = logic.amountPaid + coin.getValue().intValue();
		} catch (DisabledException e) {
			
			e.printStackTrace();
		} catch (OverloadException e) {
			
			e.printStackTrace();
		}
	}
	
	private void payToonie()
	{
		BigDecimal value = new BigDecimal("2.00");
		Coin coin = new Coin(value);
		try {
			logic.station.coinSlot.accept(coin);
			logic.amountPaid = logic.amountPaid + coin.getValue().intValue();
		} catch (DisabledException e) {
			
			e.printStackTrace();
		} catch (OverloadException e) {
			
			e.printStackTrace();
		}
	}
	
	private void payFive()
	{
		Currency currency = Currency.getInstance("CAD");
		Banknote banknote = new Banknote(currency, 5);
		try {
			logic.station.banknoteInput.accept(banknote);
			logic.amountPaid = logic.amountPaid + banknote.getValue();
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
	}
	
	private void payTen()
	{
		Currency currency = Currency.getInstance("CAD");
		Banknote banknote = new Banknote(currency, 10);
		try {
			logic.station.banknoteInput.accept(banknote);
			logic.amountPaid = logic.amountPaid + banknote.getValue();
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
	}
	
	private void payTwenty()
	{
		Currency currency = Currency.getInstance("CAD");
		Banknote banknote = new Banknote(currency, 20);
		try {
			logic.station.banknoteInput.accept(banknote);
			logic.amountPaid = logic.amountPaid + banknote.getValue();
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
	}
	
	private void payFifty()
	{
		Currency currency = Currency.getInstance("CAD");
		Banknote banknote = new Banknote(currency, 50);
		try {
			logic.station.banknoteInput.accept(banknote);
			logic.amountPaid = logic.amountPaid + banknote.getValue();
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
	}
	
	private void payHundred() 
	{
		Currency currency = Currency.getInstance("CAD");
		Banknote banknote = new Banknote(currency, 100);
		try {
			logic.station.banknoteInput.accept(banknote);
			logic.amountPaid = logic.amountPaid + banknote.getValue();
		} catch (DisabledException e) {
			e.printStackTrace();
		} catch (OverloadException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Launch the application. TO BE USED FOR TESTING ONLY!
	 */
	public static void main(String[] args) {
		
		SelfCheckoutStation scs = new SelfCheckoutStation(
				Currency.getInstance("CAD"),
				new int[] {5, 10, 15, 20, 50, 100},
				new BigDecimal[] {new BigDecimal("0.25"), new BigDecimal("0.10"), 
						new BigDecimal("0.05"), new BigDecimal("1.00"), new BigDecimal("2.00")},
				15,
				3
				);
		
		SelfCheckoutSystemLogic testlogic = new SelfCheckoutSystemLogic(scs);
		
		PriceLookupCode c1 = new PriceLookupCode("11111");
		PriceLookupCode c2 = new PriceLookupCode("22222");
		PriceLookupCode c3 = new PriceLookupCode("33333");
		PriceLookupCode c4 = new PriceLookupCode("44444");
		
		PLUCodedProduct p1 = new PLUCodedProduct(c1, "bananas (smol)", new BigDecimal("700.00"));
		PLUCodedProduct p4 = new PLUCodedProduct(c4, "bananas plantain", new BigDecimal("0.99"));
		PLUCodedProduct p2 = new PLUCodedProduct(c2, "car", new BigDecimal("2.00"));
		PLUCodedProduct p3 = new PLUCodedProduct(c3, "monke (fren)", new BigDecimal("0.01"));
		
		ProductDatabases.PLU_PRODUCT_DATABASE.put(c1, p1);
		ProductDatabases.PLU_PRODUCT_DATABASE.put(c2, p2);
		ProductDatabases.PLU_PRODUCT_DATABASE.put(c3, p3);
		ProductDatabases.PLU_PRODUCT_DATABASE.put(c4, p4);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CustomerGui gui = new CustomerGui(testlogic);
					JFrame frame = new JFrame();
					frame.setContentPane(gui);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.pack();
					frame.setVisible(true);
					gui.startup();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

}
