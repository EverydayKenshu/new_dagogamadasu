package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

@Controller
public class ClientUserDeleteController {
	@Autowired
	UserRepository userRepository;
	//会員詳細画面で退会ボタン押下時にid条件でデータベースから検索しセッションスコープに保存するメソッド
	@RequestMapping(path = "/client/user/delete/check", method = RequestMethod.POST)
	public String userDeleteCheckPost(HttpSession session) {
		//セッションから現在ログインしている一般会員の情報を取得
		User loginUser = (User)session.getAttribute("user");
		UserForm userForm = new UserForm();
		userForm.setId(loginUser.getId());//会員番号
		userForm.setName(loginUser.getName());//名前
		userForm.setEmail(loginUser.getEmail());//メールアドレス
		userForm.setPhoneNumber(loginUser.getPhoneNumber());//電話番号
		userForm.setAddress(loginUser.getAddress());//住所
		userForm.setPostalCode(loginUser.getPostalCode());//郵便番号
		session.setAttribute("userForm", userForm);
		return "redirect:/client/user/delete/check";
	}
	//セッションスコープから情報を取得し退会確認画面へと遷移させるメソッド
	@RequestMapping(path = "/client/user/delete/check", method = RequestMethod.GET)
	public String userDeleteCheckGet(HttpSession session, Model model) {
		//userFormにセッションスコープ情報をセットさせる
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		//userFormからリクエストスコープに情報を取得させる
		model.addAttribute("userForm", userForm);
		return "client/user/delete_check";
	}
	
	//退会確認画面で退会ボタン押下時にセッションスコープの内容を破棄
	@RequestMapping(path = "/client/user/delete/complete", method = RequestMethod.POST)
	public String userDeleteComplete(HttpSession session) {
		//セッションスコープから情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		User user = userRepository.findById(userForm.getId()).get();
		//DeleteFlagを1に変える
		user.setDeleteFlag(1);
		//データベースに情報を更新させる
		userRepository.save(user);
		//ログインしているuser情報を取り除く
		session.removeAttribute("userForm");
		session.invalidate();
		return "redirect:/client/user/delete/complete";
	}
	//退会確認完了画面を表示するメソッド
	@RequestMapping(path = "/client/user/delete/complete", method = RequestMethod.GET)
	public String userDeleteCompleteGet() {
		return "client/user/delete_complete";
	}

	
}
