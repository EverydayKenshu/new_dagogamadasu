package jp.co.sss.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.sss.shop.entity.Favorite;

/**
 * favoriteテーブル用リポジトリ
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

	/**
	 * 会員IDと商品IDからお気に入り情報を取得
	 */
	Favorite findByUserIdAndItemId(Integer userId, Integer itemId);

	/**
	 * 会員IDとお気に入りフラグからお気に入り一覧を取得
	 */
	List<Favorite> findByUserIdAndFavoriteFlagOrderByCreatedAtDesc(
	        Integer userId,
	        Integer favoriteFlag);
	
	Favorite findByUser_IdAndItem_Id(Integer userId, Integer itemId);
}
