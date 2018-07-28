package com.example.entity.model;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the film_text database table.
 * 
 */
@Entity
@Table(name="film_text")
@NamedQuery(name="FilmText.findAll", query="SELECT f FROM FilmText f")
public class FilmText implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="film_id")
	private short filmId;

	@Lob
	private String description;

	private String title;

	public FilmText() {
	}

	public short getFilmId() {
		return this.filmId;
	}

	public void setFilmId(short filmId) {
		this.filmId = filmId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}