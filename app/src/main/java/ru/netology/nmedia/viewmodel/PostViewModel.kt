package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
//    val data: LiveData<FeedModel>
//        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String>
        get() = _error

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
                Log.d("PostViewModel", "Посты успешно загружены")
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
                _error.value = e.message ?: "Ошибка"
                Log.e("PostViewModel", "Ошибка при загрузке постов: ${e.message}")
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(post: Post) {
                    _postCreated.value = Unit
                    loadPosts()
                    Log.d("PostViewModel", "Пост успешно сохранен")
                }

                override fun onError(e: Exception) {
                    _error.value = e.message ?: "Ошибка при сохранении поста"
                    Log.e("PostViewModel", "Ошибка при сохранении поста: ${e.message}")
                }
            })
        }
        edited.value = empty
    }

//    fun edit(post: Post) {
//        edited.value = post
//    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        repository.likeById(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(post: Post) {
                _data.value = _data.value?.copy(posts = _data.value?.posts.orEmpty().map {
                    if (it.id == id) post else it
                })
                Log.d("PostViewModel", "Пост понравился")
            }

            override fun onError(e: Exception) {
                _error.value = e.message ?: "Ошибка при получении поста"
                Log.e("PostViewModel", "Ошибка при получении поста: ${e.message}")
            }
        })
    }

    fun removeById(id: Long) {
        repository.removeById(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(result: Unit) {
                _data.value = _data.value?.copy(posts = _data.value?.posts.orEmpty().filter { it.id != id })
                Log.d("PostViewModel", "Пост успешно удален")
            }

            override fun onError(e: Exception) {
                _error.value = e.message ?: "Ошибка при удалении поста"
                Log.e("PostViewModel", "Ошибка при удалении поста: ${e.message}")
            }
        })
    }
}