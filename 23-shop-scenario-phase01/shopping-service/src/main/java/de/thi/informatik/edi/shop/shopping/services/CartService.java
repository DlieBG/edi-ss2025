package de.thi.informatik.edi.shop.shopping.services;

import java.util.Optional;
import java.util.UUID;

import de.thi.informatik.edi.shop.shopping.connectors.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.thi.informatik.edi.shop.shopping.model.Article;
import de.thi.informatik.edi.shop.shopping.model.Cart;
import de.thi.informatik.edi.shop.shopping.model.CartEntry;
import de.thi.informatik.edi.shop.shopping.repositories.CartRepository;

@Service
public class CartService {
	
	@Value("${cart.topic:cart}")
	private String topic;
	
	private CartRepository carts;
	private ArticleService articles;
	private KafkaProducer producer;

	public CartService(@Autowired CartRepository carts, @Autowired ArticleService articles, @Autowired KafkaProducer producer) {
		this.carts = carts;
		this.articles = articles;
		this.producer = producer;
	}
	
	public UUID createCart() {
		Cart entity = new Cart();
		this.carts.save(entity);
		return entity.getId();
	}

	public Optional<Cart> getCart(UUID id) {
		return this.carts.findById(id);
	}

	public void addArticle(UUID id, UUID article) {
		Optional<Cart> cart = this.getCart(id);
		if(cart.isEmpty()) {
			throw new IllegalArgumentException("Element with ID " + id.toString() + " not found");
		}
		Optional<Article> articleRef = this.articles.getArticle(article);
		if(articleRef.isEmpty()) {
			throw new IllegalArgumentException("Article with ID " + id.toString() + " not found");
		}
		CartEntry entry = cart.get().addArticle(articleRef.get());
		producer.addCartEntry(id, entry);

		this.carts.save(cart.get());
	}

	public void deleteArticle(UUID id, UUID article) {
		Optional<Cart> cart = this.getCart(id);
		if(cart.isEmpty()) {
			throw new IllegalArgumentException("Element with ID " + id.toString() + " not found");
		}
		CartEntry entry = cart.get().deleteArticle(article);
		producer.removeCartEntry(id, entry);

		this.carts.save(cart.get());
	}

	public void cartFinished(UUID id) {
		Optional<Cart> cart = this.getCart(id);
		if(cart.isEmpty()) {
			throw new IllegalArgumentException("Element with ID " + id.toString() + " not found");
		}
	}
}
