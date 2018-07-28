package com.example.entity.model;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the film_list database table.
 * 
 */
@Entity
@Table(name="film_list")
@NamedQuery(name="FilmList.findAll", query="SELECT f FROM FilmList f")
public class FilmList implements Serializable {
	private static final long serialVersionUID = 1L;

	@Lob
	private String actors;

	private String category;

	@Lob
	private String description;

	@Id
	private int fid;

	private int length;

	private BigDecimal price;

	private String rating;

	private String title;

	public FilmList() {
	}

	public String getActors() {
		return this.actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getFid() {
		return this.fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getRating() {
		return this.rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}