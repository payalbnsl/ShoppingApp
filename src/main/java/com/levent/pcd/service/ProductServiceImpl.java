package com.levent.pcd.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.levent.pcd.exception.InventoryMismatchExcpetion;
import com.levent.pcd.model.Order;
import com.levent.pcd.model.Order.OrderStatus;
import com.levent.pcd.model.Product;
import com.levent.pcd.model.ShoppingCartEntry;
import com.levent.pcd.model.ShoppingCartEntry.ItemStatus;
import com.levent.pcd.model.ShoppingCartMap;
import com.levent.pcd.model.Tailors;
import com.levent.pcd.repository.OrderRepository;
import com.levent.pcd.repository.ProductRepository;
import com.levent.pcd.utils.PriceStrategyTax;


@Service
public class ProductServiceImpl implements ProductService {
	
	@Autowired ShoppingCartMap cart;
	@Autowired
	private ProductRepository productRepository;
	@Autowired OrderRepository rep;
	@Autowired PriceStrategyTax priceStrategy;
	
	@Override
	public List<Product> findAll(int page, int limit) {
		
		return productRepository.findByInStoreGreaterThan(0, PageRequest.of(page, limit));
	}

	@Override
	public Product findBySku(String sku) {
		return productRepository.findBySku(sku);
	}
	
	@Override
	public List<Product> findProductsByCategory(String categoryName, int page, int size) {
		return productRepository.findProductsByCategoryProductNameAndInStoreGreaterThan(categoryName, 0,PageRequest.of(page, size));
	}

	@Override
	public List<Product> findProductsByName(String searchString) {
		return productRepository.findProductsByProductNameRegex(searchString);
	}
	
	@Override
	public List<Product> searchProductsByRegex(String searchString){
		return productRepository.searchProduct(searchString);
	}
	
	@Override
	public void updateProductsRemained(Product product, int inStore) {
		product.setInStore(inStore);
		productRepository.save(product);
	}
	

	@Override
	public void addProduct(Product product) {
		productRepository.save(product);
		
	}
	
	@InitBinder
	protected void imageFileBinder(WebDataBinder binder) {
		binder.registerCustomEditor(java.io.File.class, new FileEditor());
	}

	@Override
	public Product findById(String id) {
		return productRepository.findById(id).orElse(new Product());
	}
	@Override
	public void updateProduct(Product product) {
		productRepository.updateProduct(product);
		
	}
	@Override
	public void deleteProduct(String id) {
		productRepository.deleteById(id);
	}
	@Override
	public Order updateProductsRemained(String username) {
	double sum=0;
	List<ShoppingCartEntry> orderPlaced= new ArrayList<>();
	List<ShoppingCartEntry> orderCancelled= new ArrayList<>();
		//If few items in cart gets out of stock, just place order with products in store. Cart will reflect items to be out_of_stock for which order not placed.
		Iterator<ShoppingCartEntry> it= cart.getCartItems().values().iterator();
		while(it.hasNext()){
			ShoppingCartEntry entry= it.next();
			Optional<Product> p=productRepository.findById(entry.getId());
			if(!p.isPresent()) {
				throw new InventoryMismatchExcpetion("Invalid cart details!. Product is invalid"+ entry);
			}else {
				Product prod=p.get();
				if(productRepository.deductQuantity(prod.getId(), entry.getQuantity())) {
					sum+= priceStrategy.gePriceAfterTax(prod.getPrice());
					orderPlaced.add(entry);
					it.remove();
					
				}else {
					orderCancelled.add(entry);
					entry.setStatus(ItemStatus.OUT_OF_STOCK);
				}
				
			}
		}
		
		
		 return rep.insert(Order.builder().date(LocalDateTime.now()).productsPlaced(orderPlaced).productsCancelled(orderCancelled).amountDeducted(sum).status(OrderStatus.ORDER_INITIATED).username(username).build());
	}

	@Override
	public List<Product> findHisProducts(String username, int page, int limit) {
		List<Order> orders = rep.findOrdersByUsername(username, Sort.by("date").descending());
		if( !orders.isEmpty() ) {
			List<String> idList = new ArrayList<>();
			for( Order order : orders ) {
				List<ShoppingCartEntry> placed = order.getProductsPlaced();
				List<ShoppingCartEntry> canceled = order.getProductsCancelled();
				if( placed != null && !placed.isEmpty() ) {
					for(ShoppingCartEntry entry: placed) {
						idList.add(entry.getId());
					}
				}else if( canceled != null && !canceled.isEmpty()){
					for(ShoppingCartEntry entry: canceled) {
						idList.add(entry.getId());
					}
				}
			}
			List<Product> rtnList = new ArrayList<>();
			Optional<Product> product;
			int i = 0;
			for( String id : idList ) {
				if( i >= page * limit && i < (page + 1) * limit) {
					product = productRepository.findById(id);
					if(product.isPresent())
						rtnList.add(product.get());
					else
						--i;
					if( (i+1) == idList.size() && (i+1) % 6 == 0 )
						rtnList.add(null);
				}
				++i;
			}
			return rtnList;
		}
		else 
			return new ArrayList<>();
	}
	
	public List<Product> tailingResultPage( Tailors tailors, int page, int limit ){
		List<Product> rtnList = new ArrayList<>();
		List<Product> tList = tailingResult(tailors);
		int i = 0;
		for( Product p : tList ) {
			if( i >= page * limit && i < (page + 1) * limit) {
				rtnList.add(p);
				if( (i+1) == tList.size() && (i+1) % 6 == 0 )
					rtnList.add(null);
			}
			++i;
		}
		return rtnList;
	}
	
	public List<Product> tailingResult( Tailors tailors ) {
		List<String> categoryList = tailors.getCategoryList();
		List<String> searchList = tailors.getSearchList();
		if( categoryList.isEmpty() && searchList.isEmpty() )
			return new ArrayList<>();
		List<Product> rtnList = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		int i;
		int index;
		for( String sea: searchList ) {
			i = 0;
			index = 0;
			List<Product> seaList = productRepository.findProductsByProductNameRegex(sea);
			while( i < 2 && index < seaList.size() ) {
				if( !ids.contains((String)seaList.get(index).getId()) ) {
					rtnList.add(seaList.get(index));
					ids.add(seaList.get(index).getId());
					++i;
				}
				index++;
			}
		}
		for( String cat: categoryList ) {
			i = 0;
			index = 0;
			List<Product> catList = productRepository.findProductsByCategoryProductNameAndInStoreGreaterThan(cat, 0, PageRequest.of(0, 10));
			while( i < 2 && index < catList.size() ) {
				if( !ids.contains((String)catList.get(index).getId()) ) {
					rtnList.add(catList.get(index));
					ids.add(catList.get(index).getId());
					++i;
				}
				index++;
			}
		}
		return rtnList;
	}
}
