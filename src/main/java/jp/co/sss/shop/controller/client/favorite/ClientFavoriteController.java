package jp.co.sss.shop.controller.client.favorite;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.repository.FavoriteRepository;

@Controller
public class ClientFavoriteController {

	@Autowired
	FavoriteRepository favoriteRepository;

	//お気に入り一覧画面へ遷移する処理
	@RequestMapping("/client/favorite/list")
	public String showFavoriteList(HttpSession session, Model model) {

		UserBean userBean = (UserBean) session.getAttribute("user");

		List<Favorite> favorites =
				favoriteRepository.findByUserIdAndFavoriteFlagOrderByCreatedAtDesc(
						userBean.getId(), 1);

		model.addAttribute("favorites", favorites);

		return "client/favorite/list";
	}
	
	// お気に入り保存後に指定画面へ遷移する処理
	@PostMapping("/client/favorite/save-and-redirect")
	public String saveAndRedirect(String redirectUrl) {

		// 指定されたURLへリダイレクトする
		return "redirect:" + redirectUrl;
	}
}