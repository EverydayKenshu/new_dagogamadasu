package jp.co.sss.shop.interceptor;

import java.sql.Timestamp;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Favorite;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.repository.FavoriteRepository;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.UserRepository;

@Component
public class FavoriteInterceptor implements HandlerInterceptor {

	@Autowired
	FavoriteRepository favoriteRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ItemRepository itemRepository;

	// Controllerの処理が実行される前に呼ばれるメソッド
		@Override
		public boolean preHandle(
				HttpServletRequest request,
				HttpServletResponse response,
				Object handler) throws Exception {

			// リクエストパラメータからお気に入り変更内容を取得する
			String favoriteChanges =
					request.getParameter("favoriteChanges");

			// お気に入り変更内容が存在しない場合は何もせず処理を続行する
			if (favoriteChanges == null || favoriteChanges.isEmpty()) {
				return true;
			}

			// セッション情報を取得する
			HttpSession session = request.getSession();

			// セッションからログインユーザー情報を取得する
			UserBean userBean =
					(UserBean) session.getAttribute("user");

			// 未ログインの場合はDB更新せず処理を続行する
			if (userBean == null) {
				return true;
			}

			// JSON文字列をJavaオブジェクトに変換するためのObjectMapperを作成する
			ObjectMapper mapper = new ObjectMapper();

			// JSON形式のお気に入り変更内容をMapに変換する
			Map<String, Integer> changeMap =
					mapper.readValue(
							favoriteChanges,
							new TypeReference<Map<String, Integer>>() {
							});

			// 変更されたお気に入り情報を1件ずつ処理する
			for (Map.Entry<String, Integer> entry : changeMap.entrySet()) {

				// Mapのキーから商品IDを取得する
				Integer itemId = Integer.valueOf(entry.getKey());

				// Mapの値からお気に入りフラグを取得する
				Integer favoriteFlag = entry.getValue();

				// ログインユーザーIDと商品IDから既存のお気に入り情報を取得する
				Favorite favorite =
						favoriteRepository.findByUser_IdAndItem_Id(
								userBean.getId(), itemId);

				// お気に入り情報が存在しない場合
				if (favorite == null) {

					// 新しいお気に入りエンティティを作成する
					favorite = new Favorite();

					// ログインユーザーIDからユーザーエンティティを取得する
					User user = userRepository.findById(userBean.getId()).orElse(null);

					// 商品IDから商品エンティティを取得する
					Item item = itemRepository.findById(itemId).orElse(null);

					// ユーザーまたは商品が取得できない場合はこのデータの処理をスキップする
					if (user == null || item == null) {
						continue;
					}

					// お気に入りエンティティにユーザー情報を設定する
					favorite.setUser(user);

					// お気に入りエンティティに商品情報を設定する
					favorite.setItem(item);
				}
				
				// お気に入り登録状態にする場合
				if (favoriteFlag == 1) {

					// 登録日時を現在日時に更新する
					favorite.setCreatedAt(new Timestamp(System.currentTimeMillis()));
				}

				// お気に入りフラグを設定する
				favorite.setFavoriteFlag(favoriteFlag);

				// お気に入り情報をDBへ保存する
				favoriteRepository.save(favorite);
			}

			// 後続のController処理を続行する
			return true;
		}
}