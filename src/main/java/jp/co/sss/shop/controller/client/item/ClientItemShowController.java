package jp.co.sss.shop.controller.client.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.co.sss.shop.entity.Item;
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
	 * Entity、Form、Bean間のデータコピーサービス
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
	public String showItemList(@PathVariable Integer sortType, Integer categoryId, Model model, Pageable pageable) {

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

		return "client/item/list";

	}

	//詳細
	@GetMapping("/client/item/detail/{id}")
	public String showItemDetail(@PathVariable Integer id, Model model) {
		// 商品情報をViewへ渡す
		//		リポジトリからIDを取得する
		model.addAttribute("item", itemRepository.findByIdAndDeleteFlag(id, Constant.NOT_DELETED));

		return "client/item/detail";
	}
}
