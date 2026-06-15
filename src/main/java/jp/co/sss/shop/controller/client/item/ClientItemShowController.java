package jp.co.sss.shop.controller.client.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.util.Constant;

/**
 * 商品管理 一覧表示機能(一般会員用)のコントローラクラス
 *
 * @author SystemShared
 */
@Controller
public class ClientItemShowController {
	/**
	 * 商品情報
	 */
	@Autowired
	ItemRepository itemRepository;
	
	/**
	 * お気に入り情報
	 */
	@Autowired
	FavoriteRepository favoriteRepository;

	/**
	 * Entity、Form、Bean間のデータコピーサービス
	 * 
	 */
	@Autowired
	BeanTools beanTools;

	/**
	 * トップ画面 表示処理
	 *
	 * @param model    Viewとの値受渡し
	 * @return "index" トップ画面
	 */

	@RequestMapping(path = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public String showTop(Model model, Pageable pageable) {

		Page<Item> itemsPage = itemRepository.findByOrderCountDesc(Constant.NOT_DELETED, pageable);

		model.addAttribute("items", itemsPage.getContent());

		return "index";
	}

	//一覧
	@RequestMapping("/client/item/list/{sortType}")
	public String showItemList(@PathVariable Integer sortType, Integer categoryId, Model model, Pageable pageable, HttpSession session) {

		//	　エンティティ
		Page<Item> itemsPage = null;

		//		一覧表を押された時、nullで送られてきたらエラーになるため、categoryIdを０にする

		if (categoryId == null) {
			categoryId = 0;
		}

		if (sortType == 1) {

			//			一覧の新着順

			itemsPage = itemRepository.findByOrderCountDesc(Constant.NOT_DELETED, pageable);

			//			カテゴリの新着順

			if (categoryId != 0) {
				itemsPage = itemRepository.findCategoryOrderByInsertDateDesc(Constant.NOT_DELETED,
						categoryId, pageable);
			}
		}

		else if (sortType == 2) {

			//			一覧の売れ筋順

			itemsPage = itemRepository.findByDeleteFlagOrderByInsertDateDescPage(Constant.NOT_DELETED, pageable);

			//			カテゴリの売れ筋順

			if (categoryId != 0) {
				itemsPage = itemRepository.findByCategoryOrderCountDesc(Constant.NOT_DELETED,
						categoryId, pageable);
			}
		}

		// 商品情報をViewへ渡す
		model.addAttribute("items", itemsPage.getContent());
		model.addAttribute("pages", itemsPage);
		model.addAttribute("sortType", sortType);
		model.addAttribute("categoryId", categoryId);
		
		
		
		Map<Integer, Boolean> favoriteMap = new HashMap<>();

		UserBean userBean = (UserBean) session.getAttribute("user");

		if (userBean != null) {

			List<Favorite> favorites =
					favoriteRepository.findByUserIdAndFavoriteFlagOrderByCreatedAtDesc(
							userBean.getId(), 1);

			for (Favorite favorite : favorites) {
				favoriteMap.put(favorite.getItem().getId(), true);
			}
		}

		model.addAttribute("favoriteMap", favoriteMap);

		return "client/item/list";

	}

	// 詳細画面を表示するURL
	@GetMapping("/client/item/detail/{id}")
	public String showItemDetail(@PathVariable Integer id, Model model, HttpSession session) {

		// 商品IDから削除されていない商品情報を取得する
		Item item = itemRepository.findByIdAndDeleteFlag(id, Constant.NOT_DELETED);

		// 取得した商品情報を画面へ渡す
		model.addAttribute("item", item);

		// お気に入り状態の初期値をfalseにする
		boolean favorite = false;

		// セッションからログインユーザー情報を取得する
		UserBean userBean = (UserBean) session.getAttribute("user");

		// ログインしている場合だけお気に入り状態を確認する
		if (userBean != null) {

			// ユーザーIDと商品IDからお気に入り情報を取得する
			Favorite favoriteData = favoriteRepository.findByUser_IdAndItem_Id(userBean.getId(), id);

			// お気に入り情報が存在し、フラグが1の場合
			if (favoriteData != null && favoriteData.getFavoriteFlag() == 1) {

				// お気に入り状態をtrueにする
				favorite = true;
			}
		}

		// お気に入り状態を画面へ渡す
		model.addAttribute("favorite", favorite);

		// 商品詳細画面を表示する
		return "client/item/detail";
	}
	
	// 商品一覧から商品詳細へ遷移した時
	@PostMapping("/client/item/detail/{id}")
	public String showItemDetailPost(
			@PathVariable Integer id,
			Model model,
			HttpSession session,
			String backPage) {

		// 商品IDから商品情報を取得する
		Item item =
				itemRepository.findByIdAndDeleteFlag(
						id,
						Constant.NOT_DELETED);

		// 取得した商品情報を画面へ渡す
		model.addAttribute("item", item);
		
		// 戻る先情報を画面へ渡す
		model.addAttribute("backPage", backPage);

		// お気に入り状態の初期値をfalseにする
		boolean favorite = false;

		// セッションからログインユーザー情報を取得する
		UserBean userBean =
				(UserBean) session.getAttribute("user");

		// ログインしている場合のみお気に入り情報を確認する
		if (userBean != null) {

			// ユーザーIDと商品IDからお気に入り情報を取得する
			Favorite favoriteData =
					favoriteRepository.findByUser_IdAndItem_Id(
							userBean.getId(),
							id);

			// お気に入り情報が存在し、
			// かつお気に入りフラグが1の場合
			if (favoriteData != null
					&& favoriteData.getFavoriteFlag() == 1) {

				// お気に入り状態をtrueにする
				favorite = true;
			}
		}

		// お気に入り状態を画面へ渡す
		model.addAttribute("favorite", favorite);

		// 商品詳細画面を表示する
		return "client/item/detail";
	}
}
