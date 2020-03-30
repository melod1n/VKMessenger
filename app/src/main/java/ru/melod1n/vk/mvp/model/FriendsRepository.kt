package ru.melod1n.vk.mvp.model

import ru.melod1n.vk.api.VKApi
import ru.melod1n.vk.api.model.VKUser
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.database.CacheStorage
import ru.melod1n.vk.mvp.contract.FriendsContract
import ru.melod1n.vk.util.ArrayUtil

class FriendsRepository : FriendsContract.Repository<VKUser>() {
    override fun loadCachedValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean): ArrayList<VKUser> {
        val friends = CacheStorage.getFriends(id, onlyOnline)

        return ArrayUtil.cut(friends, offset, count)
    }

    override fun loadValues(id: Int, offset: Int, count: Int, onlyOnline: Boolean, listener: VKApi.OnResponseListener<VKUser>) {
        TaskManager.execute {
            try {
                val models = VKApi.friends()
                        .get()
                        .order("hints")
                        .fields(VKUser.DEFAULT_FIELDS)
                        .count(count)
                        .offset(offset)
                        .execute(VKUser::class.java) ?: ArrayList()

                insertDataInDatabase(models)
                AppGlobal.handler.post(VKApi.SuccessCallback(listener, models))
            } catch (e: Exception) {
                e.printStackTrace()
                AppGlobal.handler.post(VKApi.ErrorCallback(listener, e))
            }
        }
    }

    override fun insertDataInDatabase(models: ArrayList<VKUser>) {
        CacheStorage.insertFriends(models)
    }
}