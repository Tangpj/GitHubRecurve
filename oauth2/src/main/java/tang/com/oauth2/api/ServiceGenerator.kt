package tang.com.github.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import tang.com.oauth.api.OAuthService

fun createOauthService(): OAuthService{
    val retrofit = Retrofit.Builder().baseUrl("https://github.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
    return retrofit.create(OAuthService::class.java)

}