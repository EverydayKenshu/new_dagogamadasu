package jp.co.sss.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jp.co.sss.shop.interceptor.FavoriteInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

		/**
		 * お気に入り更新用Interceptor
		 */
		@Autowired
		FavoriteInterceptor favoriteInterceptor;

		/**
		 * Interceptorを登録するメソッド
		 */
		@Override
		public void addInterceptors(InterceptorRegistry registry) {

			// FavoriteInterceptorを登録する
			registry.addInterceptor(favoriteInterceptor)

					// /client配下のURLへアクセスした際に実行する
					.addPathPatterns("/client/**");
		}
}