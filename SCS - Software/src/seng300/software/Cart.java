package seng300.software;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.InvalidArgumentSimulationException;
import org.lsmr.selfcheckout.PriceLookupCode;
import org.lsmr.selfcheckout.external.ProductDatabases;
import org.lsmr.selfcheckout.products.BarcodedProduct;
import org.lsmr.selfcheckout.products.PLUCodedProduct;
import org.lsmr.selfcheckout.products.Product;

import seng300.software.exceptions.ProductNotFoundException;
import seng300.software.observers.CartObserver;

public class Cart
{

	
	private ProductDatabaseLogic databaseLogic;
	private List<Product> cart;


	private BigDecimal cartTotal;
	private List<CartObserver> observers;
	private double pluItemWeight; 
	
	
	public Cart()
	{
		this.databaseLogic = new ProductDatabaseLogic();
		this.cart = new ArrayList<>();
		this.cartTotal = new BigDecimal("0.00");
		this.observers = new ArrayList<>();
	}
	
	/**
	 * Getter for the cart total. 
	 * 
	 * @return the current total price of all scanned items
	 */
	public BigDecimal getCartTotal()
	{
		return this.cartTotal;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Product> getProducts()
	{
		return (ArrayList<Product>)this.cart;
	}
	
	/**
	 * 
	 */
	public void attach(CartObserver observer)
	{
		if (observer == null)
			throw new InvalidArgumentSimulationException("arguments cannot be null");
		observers.add(observer);
	}
	
	/**
	 * Adds a scanned (barcoded) item to the cart.
	 * 
	 * @param barcode
	 * 			The barcode of the scanned item.
	 * 
	 * @throws ProductNotFoundException
	 * 			Thrown when product cannto be found in database.
	 */
	public void addToCart(Barcode barcode) throws ProductNotFoundException
	{
		BarcodedProduct p = databaseLogic.getProduct(barcode);
		cart.add(p); // add product to cart
		this.cartTotal = this.cartTotal.add(p.getPrice()); // update cart total
		// notify baggingAreaPbservers the barcode was scanned
		// and product was successfully added to the cart -- expect weight change
		notifyProductAdded(p);
//		this.baggingAreaObserver.notifiedItemAdded(p);
	}
	
	
	public void addPLUCodedProductToCart(PriceLookupCode PLUCode, double Weight) throws ProductNotFoundException
	{
		PLUCodedProduct pluProduct = databaseLogic.getPLUCodedProduct(PLUCode);
		cart.add(pluProduct); // add product to cart
		this.cartTotal = this.cartTotal.add(pluProduct.getPrice()); // update cart total
		pluItemWeight = Weight;
		// notify baggingAreaPbservers the barcode was scanned
		// and product was successfully added to the cart -- expect weight change
		notifyPLUProductAdded(pluProduct, Weight);
//		this.baggingAreaObserver.notifiedItemAdded(p);
	}
	
	
	private void notifyProductAdded(BarcodedProduct p)
	{
		for (CartObserver obs : observers)
			obs.notifyProductAdded(this, p);
	}
	
	private void notifyPLUProductAdded(PLUCodedProduct PLUProduct, double weight)
	{
		for (CartObserver obs : observers)
			obs.notifyPLUProductAdded(this, PLUProduct, weight);
	}
	
	
	public double getPLUWeight() {
		return pluItemWeight;
	}

}
