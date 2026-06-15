package jp.co.sss.shop.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * お気に入り情報のエンティティクラス
 */
@Entity
@Table(name = "favorite")
public class Favorite {

	/**
	 * お気に入りID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "favorite_seq_gen")
	@SequenceGenerator(name = "favorite_seq_gen", sequenceName = "favorite_seq", allocationSize = 1)
	private Integer id;

	/**
	 * 会員情報
	 */
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	/**
	 * 商品情報
	 */
	@ManyToOne
	@JoinColumn(name = "item_id", referencedColumnName = "id")
	private Item item;

	/**
	 * 登録日
	 */
	@Column
	private Timestamp createdAt;

	/**
	 * お気に入りフラグ
	 */
	@Column
	private Integer favoriteFlag;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getFavoriteFlag() {
		return favoriteFlag;
	}

	public void setFavoriteFlag(Integer favoriteFlag) {
		this.favoriteFlag = favoriteFlag;
	}
}
