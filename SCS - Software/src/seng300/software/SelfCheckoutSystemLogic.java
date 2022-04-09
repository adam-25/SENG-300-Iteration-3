package seng300.software;

import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.ReceiptPrinterObserver;
import org.lsmr.selfcheckout.products.BarcodedProduct;

import seng300.software.ProductDatabase;
import seng300.software.Cart;
import seng300.software.observers.BaggingAreaObserver;
import seng300.software.observers.CartObserver;
import seng300.software.observers.PrinterObserver;
import seng300.software.observers.ScannerObserver;

/**
 * Central logic for self checkout station functionalities.
 * Handles scanning an item, updating the total bill,
 * and some inter-device communication.
 * 
 *
 */
public class SelfCheckoutSystemLogic
{
	public final ProductDatabase		productDatabase; 	// products sold in store
	public final SelfCheckoutStation	station;			// station hardware
	public final Checkout 				checkout;			// checkout functionality
	// Checkout made 'public final' so that the payment methods can be easily accessed
	// instead of having to make wrapper methods for all of them.

	// Attached observers -- handle communication between hardware devices and logic
	private ScannerObserver				mainScannerObserver, handheldScannerObserver;
	private ReceiptPrinterObserver		printerObserver;
	private BaggingAreaObserver			baggingAreaObserver;
	private double 						baggingAreaSensitivity;
	// Flags related to customer functionalities - scan, bag, checkout
	private boolean usingOwnBags	= false;
	private boolean blocked			= false; // used to simulate blocking the system
	private boolean isCheckingOut	= false;
	// Cart to track items scanned and observer to pass messages
	private Cart			cart;
	private CartObserver	cartObserver; 
	/**
	 * Basic constructor
	 * 
	 * @param scs
	 * 			Self checkout station to install logic on.
	 * @param database
	 * 			Connection to database of products in available in store.
	 */
	public SelfCheckoutSystemLogic(SelfCheckoutStation scs, ProductDatabase database) // take pin to unblock station as input?
			throws NullPointerException
	{
		if (scs == null || database == null)
			throw new NullPointerException("arguments cannot be null");
		
		this.station 			= scs;
		this.productDatabase 	= database;

		this.printerObserver = new PrinterObserver(this);
		this.station.printer.attach(printerObserver);
		
		this.baggingAreaSensitivity	= this.station.baggingArea.getSensitivity();
		this.baggingAreaObserver	= new BaggingAreaObserver(this);
		this.station.baggingArea.attach(baggingAreaObserver);
		
		this.cartObserver = new CartObserver(this.baggingAreaObserver);
		this.cart = new Cart(this.productDatabase);
		this.cart.attach(cartObserver);
		
		this.mainScannerObserver = new ScannerObserver(this.cart);
		this.station.mainScanner.attach(mainScannerObserver);
		
		this.handheldScannerObserver = new ScannerObserver(this.cart);
		this.station.handheldScanner.attach(handheldScannerObserver);
		
		this.checkout = new Checkout(station, this.cart.getProducts(), this.cart.getCartTotal());
	}
	
	/**
	 * Starts the checkout process. Called when customer
	 * indicates they want to checkout (e.g. by pressing a checkout button).
	 * 
	 */
	public void wantsToCheckout()
	{
		// disable scanners
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		isCheckingOut = true;
		// update cart and price
		checkout.update(this.cart.getCartTotal());
	}
	
	/**
	 * Lets customer add item after partial payment
	 * mid-way through checkout process.
	 * May only be called before 
	 * checkout.finishPayment() is called.
	 * To return to checkout, call wantToCheckout();
	 */
	public void addItemAfterCheckoutStart()
	{
		// enable scanners again
		this.station.mainScanner.enable();
		this.station.handheldScanner.enable();
		isCheckingOut = false;
	}
	
	/**
	 * Wrapper for this.checkout.finishPayment() method.
	 */
	public void finishCheckout()
	{
		if (isCheckingOut)
			this.checkout.finishPayment();
		// ignore attempts to finish checking out
		// if checkout has not been started
		// you wouldn't realistically be able to get the the 
		// 'Finish Checkout' button if you do not click the 
		// 'Start Checkout' button.
	}
	
//	public void returnToCheckout()
//	{
//		this.station.mainScanner.disable();
//		this.station.handheldScanner.disable();
//		// update cart and price
//		checkout.update();
//	}
	
	/**
	 * Getter for bagging area scale sensitivity.
	 * 
	 * @return sensitivity of bagging area scale
	 */
	public double getBaggingAreaSensitivity()
	{
		return baggingAreaSensitivity;
	}
	
	/**
	 * Simulates customer wanting to remove an item from the bagging area 
	 * (but still wanting to pay for it)
	 */
	public void selectItemToRemove(BarcodedProduct someProduct) {
		this.baggingAreaObserver.setBaggingItems(false);
		this.baggingAreaObserver.wishesToRemoveItem(someProduct);
	}
	
	/**
	 * Simulates going back to normal operation after removing
	 * an item from the bagging area. 
	 */
	public void returnToNormalBaggingOperation() {
		this.baggingAreaObserver.setBaggingItems(true);
	}
	
	/**
	 * Simulates process taken when user indicates they
	 * want to use their own bags during checkout.
	 */
	public void useOwnBags()
	{
		usingOwnBags = true;
		block();
		// attendant station will unblock system...
	}
	
	/**
	 * Returns whether the system is currently blocked.
	 * 
	 * @return true if system is blocked; else, false.
	 */
	public boolean isBlocked()
	{
		return blocked;
	}
	
	/**
	 * Blocks the system so customers cannot continue scanning/checkout.
	 */
	public void block()
	{
		blocked = true;
		// disable the scanners
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		// TODO: The scales should remain enabled but do we need to disable any other devices?
		// a GUI would probably show up a really annoying error
	}

	/**
	 * Blocks the system so customers cannot continue scanning/checkout, it is the same as block() except makes an additional call to notify the attendant 
	 */
	public void blockUnexpectedWeight()
	{
		blocked = true;
		// disable the scanners
		this.station.mainScanner.disable();
		this.station.handheldScanner.disable();
		// TODO: The scales should remain enabled but do we need to disable any other devices?
		// a GUI would probably show up a really annoying error
		//Makes a call to the attendant to transfer control of logic or maybe pinging an observer
		//Waiting to see how exactly attendant logic will work 
	}

	/**
	 * Unblocks the system so customer can continue scanning/checkout.
	 */
	public void unblock() // take pin as parameter?
	{
		// validate pin?
		blocked = false;
		// enable the scanners
		this.station.mainScanner.enable();
		this.station.handheldScanner.enable();
	}
	
	public Cart getCart() {
		return this.cart;
	}

	public ArrayList<BarcodedProduct> getBaggedProducts(){
        	return this.baggingAreaObserver.getBaggedProducts();
    	}
	
}
